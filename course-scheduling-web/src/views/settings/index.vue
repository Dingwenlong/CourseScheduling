<template>
  <PageContainer with-tabbar class="settings-page">
    <PageHeader title="系统设置" subtitle="通过数据适配层同步旧教务系统中的课程、班级和教室信息">
      <n-button secondary @click="loadPreset(provider)">
        载入预设映射
      </n-button>
      <n-button type="primary" :loading="previewLoading" @click="handlePreview">
        预览同步
      </n-button>
      <n-button type="warning" :loading="applyLoading" @click="handleApply">
        执行同步
      </n-button>

      <template #description>
        同步过程在后台完成，管理员可以先查看新增、更新和跳过数量，再决定是否正式写入，避免和现有课表数据误覆盖。
      </template>
    </PageHeader>

    <div class="settings-grid">
      <section class="panel config-panel">
        <div class="panel-header">
          <div>
            <h3>数据对接配置</h3>
            <p>支持通过字段映射适配不同旧教务系统的数据结构。</p>
          </div>
          <n-tag type="info" round>{{ providerLabel }}</n-tag>
        </div>

        <n-form label-placement="top" class="config-form">
          <div class="form-grid">
            <n-form-item label="数据来源">
              <n-select v-model:value="provider" :options="providerOptions" @update:value="loadPreset" />
            </n-form-item>
            <n-form-item label="默认院系">
              <n-select
                v-model:value="defaultDeptId"
                clearable
                filterable
                :options="departmentOptions"
                placeholder="当同步数据未提供院系ID时使用"
              />
            </n-form-item>
            <n-form-item label="默认校区">
              <n-select
                v-model:value="defaultCampusId"
                clearable
                filterable
                :options="campusOptions"
                placeholder="当教室数据未提供校区ID时使用"
              />
            </n-form-item>
          </div>

          <n-form-item label="字段映射 JSON">
            <n-input
              v-model:value="mappingText"
              type="textarea"
              :autosize="{ minRows: 14, maxRows: 22 }"
              placeholder="请输入字段映射 JSON"
            />
          </n-form-item>

          <n-form-item label="待同步数据 JSON">
            <n-input
              v-model:value="payloadText"
              type="textarea"
              :autosize="{ minRows: 16, maxRows: 26 }"
              placeholder="请输入课程、班级、教室数据 JSON"
            />
          </n-form-item>
        </n-form>
      </section>

      <section class="panel side-panel">
        <div class="panel-header">
          <div>
            <h3>同步说明</h3>
            <p>只覆盖开题报告里提到的数据对接范围，不扩展到额外模块。</p>
          </div>
        </div>

        <div class="guide-list">
          <div class="guide-item">
            <div class="guide-index">01</div>
            <div>
              <div class="guide-title">先选适配预设</div>
              <div class="guide-text">可从正方、青果或自定义模板开始，再按学校字段名微调。</div>
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-index">02</div>
            <div>
              <div class="guide-title">先预览再执行</div>
              <div class="guide-text">系统会先统计哪些行会新增、更新或被跳过，降低误同步风险。</div>
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-index">03</div>
            <div>
              <div class="guide-title">默认值兜底</div>
              <div class="guide-text">旧系统缺少院系或校区字段时，可用默认院系、默认校区补齐必要信息。</div>
            </div>
          </div>
        </div>

        <div class="preset-card">
          <div class="preset-title">当前预设包含</div>
          <div class="preset-tags">
            <n-tag round>课程同步</n-tag>
            <n-tag round>班级同步</n-tag>
            <n-tag round>教室同步</n-tag>
            <n-tag round type="success">字段映射</n-tag>
          </div>
        </div>

        <div v-if="applyResult" class="result-card">
          <div class="result-title">最近一次执行结果</div>
          <div class="result-total">
            <div class="metric-item">
              <span class="metric-value">{{ applyResult.totalCreated || 0 }}</span>
              <span class="metric-label">新增</span>
            </div>
            <div class="metric-item">
              <span class="metric-value">{{ applyResult.totalUpdated || 0 }}</span>
              <span class="metric-label">更新</span>
            </div>
            <div class="metric-item">
              <span class="metric-value">{{ applyResult.totalSkipped || 0 }}</span>
              <span class="metric-label">跳过</span>
            </div>
          </div>
          <div class="result-time">
            执行时间：{{ formatTime(applyResult.syncTime) }}
          </div>
        </div>
      </section>
    </div>

    <section class="preview-section">
      <div class="section-heading">
        <div>
          <h3>同步预览</h3>
          <p>预览结果会按课程、班级、教室分别统计。</p>
        </div>
        <n-tag v-if="previewResult?.previewTime" type="success" round>
          {{ formatTime(previewResult.previewTime) }}
        </n-tag>
      </div>

      <div v-if="previewDatasets.length" class="preview-grid">
        <article v-for="dataset in previewDatasets" :key="dataset.dataset" class="preview-card">
          <div class="preview-card-header">
            <div>
              <h4>{{ dataset.label }}</h4>
              <p>共 {{ dataset.totalCount || 0 }} 条待处理数据</p>
            </div>
            <n-tag round :type="dataset.readyCount ? 'success' : 'default'">
              就绪 {{ dataset.readyCount || 0 }}
            </n-tag>
          </div>

          <div class="metric-row">
            <div class="metric-item">
              <span class="metric-value">{{ dataset.createCount || 0 }}</span>
              <span class="metric-label">新增</span>
            </div>
            <div class="metric-item">
              <span class="metric-value">{{ dataset.updateCount || 0 }}</span>
              <span class="metric-label">更新</span>
            </div>
            <div class="metric-item">
              <span class="metric-value">{{ dataset.skippedCount || 0 }}</span>
              <span class="metric-label">跳过</span>
            </div>
          </div>

          <div class="card-block">
            <div class="block-title">预览样例</div>
            <div v-if="dataset.sampleRows?.length" class="sample-list">
              <div v-for="(sample, sampleIndex) in dataset.sampleRows" :key="sampleIndex" class="sample-item">
                <div v-for="(value, key) in sample" :key="key" class="sample-pair">
                  <span class="sample-key">{{ key }}</span>
                  <span class="sample-value">{{ value }}</span>
                </div>
              </div>
            </div>
            <n-empty v-else description="暂无可展示样例" />
          </div>

          <div class="card-block">
            <div class="block-title">校验提醒</div>
            <ul v-if="dataset.warnings?.length" class="warning-list">
              <li v-for="warning in dataset.warnings" :key="warning">{{ warning }}</li>
            </ul>
            <div v-else class="ok-text">当前数据未发现阻断性问题。</div>
          </div>
        </article>
      </div>

      <div v-else class="empty-panel">
        <n-empty description="还没有生成同步预览，先点上面的“预览同步”看看结果。" />
      </div>
    </section>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import {
  NButton,
  NEmpty,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NTag,
  useMessage
} from 'naive-ui'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { searchCampuses, searchDepartments } from '@/api/lookup'
import { applyDataSync, previewDataSync } from '@/api/settings'

const message = useMessage()

const providerOptions = [
  { label: '正方教务', value: 'ZHENGFANG' },
  { label: '青果教务', value: 'QINGGUO' },
  { label: '自定义映射', value: 'CUSTOM' }
]

const presetMappings = {
  ZHENGFANG: {
    courses: {
      code: 'kch',
      name: 'kcmc',
      type: 'kclb',
      credit: 'xf',
      theoryHours: 'llxs',
      practiceHours: 'sjxs',
      totalHours: 'zxs',
      deptId: 'yxbh',
      priority: 'yxj',
      needMultimedia: 'dmt',
      needLab: 'sy',
      status: 'zt'
    },
    classes: {
      code: 'bjdm',
      name: 'bjmc',
      grade: 'nj',
      studentCount: 'rs',
      counselorName: 'bzr',
      counselorPhone: 'bzrdh',
      deptId: 'yxbh',
      status: 'zt'
    },
    classrooms: {
      roomNo: 'jsh',
      roomName: 'jsmc',
      building: 'jxl',
      floor: 'lc',
      capacity: 'rl',
      roomType: 'jslx',
      hasProjector: 'txy',
      hasMicrophone: 'mkf',
      hasAirConditioner: 'kt',
      equipmentDesc: 'sbsm',
      campusId: 'xqid',
      status: 'zt'
    }
  },
  QINGGUO: {
    courses: {
      code: 'courseCode',
      name: 'courseName',
      type: 'courseType',
      credit: 'credit',
      theoryHours: 'theoryHours',
      practiceHours: 'practiceHours',
      totalHours: 'totalHours',
      deptId: 'departmentId',
      priority: 'priority',
      needMultimedia: 'needMultimedia',
      needLab: 'needLab',
      status: 'status'
    },
    classes: {
      code: 'classCode',
      name: 'className',
      grade: 'grade',
      studentCount: 'studentCount',
      counselorName: 'counselorName',
      counselorPhone: 'counselorPhone',
      deptId: 'departmentId',
      status: 'status'
    },
    classrooms: {
      roomNo: 'roomNo',
      roomName: 'roomName',
      building: 'building',
      floor: 'floor',
      capacity: 'capacity',
      roomType: 'roomType',
      hasProjector: 'hasProjector',
      hasMicrophone: 'hasMicrophone',
      hasAirConditioner: 'hasAirConditioner',
      equipmentDesc: 'equipmentDesc',
      campusId: 'campusId',
      status: 'status'
    }
  },
  CUSTOM: {
    courses: {
      code: 'code',
      name: 'name',
      type: 'type',
      credit: 'credit',
      theoryHours: 'theoryHours',
      practiceHours: 'practiceHours',
      totalHours: 'totalHours',
      deptId: 'deptId',
      status: 'status'
    },
    classes: {
      code: 'code',
      name: 'name',
      grade: 'grade',
      studentCount: 'studentCount',
      deptId: 'deptId',
      status: 'status'
    },
    classrooms: {
      roomNo: 'roomNo',
      roomName: 'roomName',
      building: 'building',
      floor: 'floor',
      capacity: 'capacity',
      roomType: 'roomType',
      campusId: 'campusId',
      status: 'status'
    }
  }
}

const presetPayloads = {
  ZHENGFANG: {
    courses: [{ kch: 'CS101', kcmc: '数据结构', xf: 3, kclb: '必修', yxbh: 1, llxs: 32, sjxs: 16, zt: 1 }],
    classes: [{ bjdm: 'CS2301', bjmc: '软件工程2301', nj: '2023', rs: 42, yxbh: 1, zt: 1 }],
    classrooms: [{ jsh: 'A101', jsmc: '致知楼A101', jxl: '致知楼', lc: 1, rl: 80, jslx: '多媒体', xqid: 1, zt: 1 }]
  },
  QINGGUO: {
    courses: [{ courseCode: 'CS101', courseName: '数据结构', credit: 3, courseType: 'REQUIRED', departmentId: 1, theoryHours: 32, practiceHours: 16, status: 1 }],
    classes: [{ classCode: 'CS2301', className: '软件工程2301', grade: '2023', studentCount: 42, departmentId: 1, status: 1 }],
    classrooms: [{ roomNo: 'A101', roomName: '致知楼A101', building: '致知楼', floor: 1, capacity: 80, roomType: 'MULTIMEDIA', campusId: 1, status: 1 }]
  },
  CUSTOM: {
    courses: [{ code: 'CS101', name: '数据结构', credit: 3, type: 'REQUIRED', deptId: 1, theoryHours: 32, practiceHours: 16, status: 1 }],
    classes: [{ code: 'CS2301', name: '软件工程2301', grade: '2023', studentCount: 42, deptId: 1, status: 1 }],
    classrooms: [{ roomNo: 'A101', roomName: '致知楼A101', building: '致知楼', floor: 1, capacity: 80, roomType: 'MULTIMEDIA', campusId: 1, status: 1 }]
  }
}

const provider = ref('CUSTOM')
const defaultDeptId = ref(null)
const defaultCampusId = ref(null)
const mappingText = ref('')
const payloadText = ref('')
const previewLoading = ref(false)
const applyLoading = ref(false)
const previewResult = ref(null)
const applyResult = ref(null)
const departmentOptions = ref([])
const campusOptions = ref([])

const providerLabel = computed(() => providerOptions.find(item => item.value === provider.value)?.label || '自定义映射')
const previewDatasets = computed(() => previewResult.value?.datasets || [])

const loadPreset = (presetProvider) => {
  const currentProvider = presetProvider || provider.value
  const mappings = presetMappings[currentProvider] || presetMappings.CUSTOM
  const payload = presetPayloads[currentProvider] || presetPayloads.CUSTOM
  mappingText.value = JSON.stringify(mappings, null, 2)
  payloadText.value = JSON.stringify(payload, null, 2)
}

const formatTime = (value) => {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

const buildRequestBody = () => {
  let mappings
  let payload
  try {
    mappings = JSON.parse(mappingText.value || '{}')
  } catch (error) {
    throw new Error('字段映射 JSON 解析失败，请检查格式')
  }
  try {
    payload = JSON.parse(payloadText.value || '{}')
  } catch (error) {
    throw new Error('待同步数据 JSON 解析失败，请检查格式')
  }

  return {
    provider: provider.value,
    defaultDeptId: defaultDeptId.value,
    defaultCampusId: defaultCampusId.value,
    mappings,
    payload
  }
}

const handlePreview = async () => {
  try {
    previewLoading.value = true
    const res = await previewDataSync(buildRequestBody())
    previewResult.value = res.data
    message.success('同步预览已生成')
  } catch (error) {
    previewResult.value = null
    message.error(error.message || '同步预览失败')
  } finally {
    previewLoading.value = false
  }
}

const handleApply = async () => {
  try {
    applyLoading.value = true
    const res = await applyDataSync(buildRequestBody())
    applyResult.value = res.data
    message.success(`同步完成：新增 ${res.data?.totalCreated || 0} 条，更新 ${res.data?.totalUpdated || 0} 条`)
    await handlePreview()
  } catch (error) {
    applyResult.value = null
    message.error(error.message || '执行同步失败')
  } finally {
    applyLoading.value = false
  }
}

const loadLookupOptions = async () => {
  try {
    const [departmentRes, campusRes] = await Promise.all([
      searchDepartments({ limit: 50 }),
      searchCampuses({ limit: 50 })
    ])
    departmentOptions.value = (departmentRes.data || []).map(item => ({
      label: item.label,
      value: item.value
    }))
    campusOptions.value = (campusRes.data || []).map(item => ({
      label: item.label,
      value: item.value
    }))
  } catch (error) {
    departmentOptions.value = []
    campusOptions.value = []
  }
}

onMounted(async () => {
  loadPreset(provider.value)
  await loadLookupOptions()
})
</script>

<style scoped>
.settings-page {
  position: relative;
}

.settings-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  gap: 20px;
}

.panel {
  position: relative;
  padding: 24px;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(252, 247, 240, 0.9)),
    radial-gradient(circle at top right, rgba(111, 137, 163, 0.12), transparent 32%);
  border: 1px solid rgba(145, 120, 91, 0.12);
  box-shadow: 0 18px 40px rgba(120, 98, 72, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.panel-header h3,
.section-heading h3,
.preview-card-header h4 {
  margin: 0;
  color: var(--text-primary);
}

.panel-header p,
.section-heading p,
.preview-card-header p,
.guide-text,
.result-time {
  margin: 6px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.config-form {
  display: grid;
  gap: 4px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.guide-list {
  display: grid;
  gap: 14px;
}

.guide-item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
  padding: 14px 16px;
  border-radius: 20px;
  background: rgba(255, 251, 245, 0.9);
  border: 1px solid rgba(145, 120, 91, 0.08);
}

.guide-index {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, #728967, #9ba980);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.guide-title,
.preset-title,
.result-title,
.block-title {
  font-weight: 700;
  color: var(--text-primary);
}

.preset-card,
.result-card {
  margin-top: 18px;
  padding: 18px;
  border-radius: 22px;
  background: rgba(250, 244, 235, 0.86);
  border: 1px solid rgba(145, 120, 91, 0.1);
}

.preset-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.preview-section {
  margin-top: 22px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.preview-card {
  padding: 22px;
  border-radius: 26px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(250, 246, 240, 0.92)),
    radial-gradient(circle at top left, rgba(184, 102, 89, 0.08), transparent 28%);
  border: 1px solid rgba(145, 120, 91, 0.12);
  box-shadow: 0 14px 30px rgba(120, 98, 72, 0.08);
}

.preview-card-header,
.metric-row,
.result-total {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.metric-row,
.result-total {
  margin-top: 18px;
}

.metric-item {
  flex: 1;
  min-width: 0;
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 250, 243, 0.92);
  border: 1px solid rgba(145, 120, 91, 0.08);
  text-align: center;
}

.metric-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.metric-label {
  display: block;
  margin-top: 6px;
  color: var(--text-secondary);
}

.card-block {
  margin-top: 18px;
}

.sample-list {
  display: grid;
  gap: 12px;
  margin-top: 10px;
}

.sample-item {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  border-radius: 18px;
  background: rgba(255, 251, 245, 0.92);
}

.sample-pair {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(145, 120, 91, 0.08);
}

.sample-key {
  color: var(--text-secondary);
}

.sample-value {
  color: var(--text-primary);
  font-weight: 600;
}

.warning-list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #9d513f;
  line-height: 1.8;
}

.ok-text,
.empty-panel {
  margin-top: 10px;
  color: var(--text-secondary);
}

.empty-panel {
  padding: 30px 18px;
  border-radius: 24px;
  background: rgba(255, 251, 245, 0.74);
  border: 1px dashed rgba(145, 120, 91, 0.18);
}

@media (max-width: 1200px) {
  .settings-grid,
  .preview-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .panel,
  .preview-card {
    padding: 18px;
    border-radius: 22px;
  }

  .panel-header,
  .section-heading,
  .preview-card-header,
  .metric-row,
  .result-total {
    flex-direction: column;
  }

  .guide-item {
    grid-template-columns: 1fr;
  }
}
</style>
