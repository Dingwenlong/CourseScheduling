<template>
  <div class="page">
    <van-nav-bar title="课表详情" left-arrow @click-left="goBack">
      <template #right>
        <van-icon name="ellipsis" size="20" @click="showActions = true" />
      </template>
    </van-nav-bar>

    <van-loading v-if="loading" class="loading-container" />

    <template v-else-if="timetable">
      <div class="card">
        <div class="flex-between">
          <div>
            <div class="page-title">{{ timetable.name }}</div>
            <div class="text-muted">{{ timetable.semester }} · 第{{ timetable.version }}版</div>
          </div>
          <van-tag :type="getStatusType(timetable.status)" size="large">
            {{ getStatusText(timetable.status) }}
          </van-tag>
        </div>

        <van-grid :column-num="4" :border="false" class="mt-16">
          <van-grid-item>
            <div class="stat-value">{{ timetable.taskCount }}</div>
            <div class="stat-label">任务数</div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-value text-success">{{ timetable.scheduledCount }}</div>
            <div class="stat-label">已排课</div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-value text-danger">{{ timetable.conflictCount }}</div>
            <div class="stat-label">冲突</div>
          </van-grid-item>
          <van-grid-item>
            <div class="stat-value">{{ timetable.utilizationRate ? timetable.utilizationRate.toFixed(1) : 0 }}%</div>
            <div class="stat-label">利用率</div>
          </van-grid-item>
        </van-grid>
      </div>

      <van-tabs v-model:active="activeTab" sticky>
        <van-tab title="课表视图">
          <div class="card">
            <div class="timetable-grid">
              <div class="timetable-header"></div>
              <div v-for="day in 5" :key="'h'+day" class="timetable-header">
                周{{ ['一', '二', '三', '四', '五'][day - 1] }}
              </div>
              <template v-for="slot in 10" :key="'s'+slot">
                <div class="timetable-header">{{ slot }}</div>
                <div
                  v-for="day in 5"
                  :key="'c'+day+'-'+slot"
                  class="timetable-cell"
                  :class="{
                    'has-course': getCourse(day, slot),
                    'conflict': getCourse(day, slot)?.isConflict
                  }"
                  @click="showCourseInfo(day, slot)"
                >
                  <div v-if="getCourse(day, slot)" class="course-block">
                    <div class="course-block-name">{{ getCourse(day, slot).courseName }}</div>
                    <div class="course-block-info">
                      {{ getCourse(day, slot).classroomName }}
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </van-tab>

        <van-tab title="课程列表">
          <van-list>
            <van-cell
              v-for="detail in details"
              :key="detail.id"
              :title="detail.courseName"
              :label="`周${detail.dayOfWeek} 第${detail.slotNo}节 · ${detail.classroomName}`"
              is-link
              @click="showDetailInfo(detail)"
            >
              <template #value>
                <van-tag v-if="detail.isConflict" type="danger">冲突</van-tag>
              </template>
            </van-cell>
          </van-list>
        </van-tab>

        <van-tab :title="'冲突(' + conflicts.length + ')'">
          <div v-if="conflicts.length === 0" class="empty-container">
            <van-icon name="passed" class="empty-icon" color="#07c160" />
            <div>暂无冲突</div>
          </div>
          <van-list v-else>
            <van-cell
              v-for="conflict in conflicts"
              :key="conflict.id"
              :title="conflict.courseName"
              :label="conflict.conflictInfo"
            >
              <template #value>
                <van-tag type="danger">冲突</van-tag>
              </template>
            </van-cell>
          </van-list>
        </van-tab>
      </van-tabs>
    </template>

    <van-action-sheet
      v-model:show="showActions"
      :actions="actions"
      cancel-text="取消"
      @select="onActionSelect"
    />

    <van-popup v-model:show="showCoursePopup" position="bottom" round style="height: 40%;">
      <div class="course-popup" v-if="currentCourse">
        <div class="page-title">{{ currentCourse.courseName }}</div>
        <van-cell-group inset>
          <van-cell title="教师" :value="currentCourse.teacherName" />
          <van-cell title="班级" :value="currentCourse.className" />
          <van-cell title="教室" :value="currentCourse.classroomName" />
          <van-cell title="时间" :value="`周${currentCourse.dayOfWeek} 第${currentCourse.slotNo}节`" />
        </van-cell-group>
        <div class="course-popup-btn">
          <van-button round block type="primary" @click="goAdjustment">
            申请调课
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getTimetableById, getTimetableDetails, getConflicts, publishTimetable, archiveTimetable, deleteTimetable } from '@/api/timetable'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const timetable = ref(null)
const details = ref([])
const conflicts = ref([])
const activeTab = ref(0)
const showActions = ref(false)
const showCoursePopup = ref(false)
const currentCourse = ref(null)

const actions = computed(() => {
  const list = []
  if (timetable.value?.status === 'DRAFT') {
    list.push({ name: '发布课表', value: 'publish' })
  }
  if (timetable.value?.status !== 'ARCHIVED') {
    list.push({ name: '归档课表', value: 'archive' })
  }
  if (timetable.value?.status === 'DRAFT') {
    list.push({ name: '删除课表', value: 'delete', color: '#ee0a24' })
  }
  return list
})

const getStatusType = (status) => {
  const map = { 'DRAFT': 'warning', 'PUBLISHED': 'success', 'ARCHIVED': 'default' }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = { 'DRAFT': '草稿', 'PUBLISHED': '已发布', 'ARCHIVED': '已归档' }
  return map[status] || status
}

const getCourse = (day, slot) => {
  return details.value.find(d => d.dayOfWeek === day && d.slotNo === slot)
}

const showCourseInfo = (day, slot) => {
  const course = getCourse(day, slot)
  if (course) {
    currentCourse.value = course
    showCoursePopup.value = true
  }
}

const showDetailInfo = (detail) => {
  currentCourse.value = detail
  showCoursePopup.value = true
}

const goAdjustment = () => {
  showCoursePopup.value = false
  router.push({
    path: '/adjustment',
    query: { timetableId: route.params.id, detailId: currentCourse.value.id }
  })
}

const onActionSelect = async (action) => {
  if (action.value === 'publish') {
    await showConfirmDialog({ title: '确认发布', message: '发布后课表将对外可见，确定发布吗？' })
    await publishTimetable(route.params.id)
    showToast('发布成功')
    loadData()
  } else if (action.value === 'archive') {
    await showConfirmDialog({ title: '确认归档', message: '归档后课表将不可修改，确定归档吗？' })
    await archiveTimetable(route.params.id)
    showToast('归档成功')
    loadData()
  } else if (action.value === 'delete') {
    await showConfirmDialog({ title: '确认删除', message: '删除后数据将无法恢复，确定删除吗？' })
    await deleteTimetable(route.params.id)
    showToast('删除成功')
    router.back()
  }
}

const goBack = () => router.back()

const loadData = async () => {
  loading.value = true
  try {
    const [timetableRes, detailsRes, conflictsRes] = await Promise.all([
      getTimetableById(route.params.id),
      getTimetableDetails(route.params.id),
      getConflicts(route.params.id)
    ])
    timetable.value = timetableRes.data
    details.value = detailsRes.data
    conflicts.value = conflictsRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #323233;
}

.stat-label {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.course-popup {
  padding: 20px 16px;
}

.course-popup-btn {
  margin-top: 20px;
  padding: 0 8px;
}
</style>
