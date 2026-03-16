export function createNaiveThemeOverrides(isDark) {
  if (isDark) {
    return {
      common: {
        fontFamily:
          '"Noto Sans SC", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
        fontFamilyMono:
          '"Cascadia Code", "SFMono-Regular", Consolas, "Liberation Mono", monospace',
        primaryColor: '#8fa286',
        primaryColorHover: '#a3b49c',
        primaryColorPressed: '#798d72',
        primaryColorSuppl: '#8fa286',
        infoColor: '#8ea4bc',
        infoColorHover: '#a4b8cd',
        infoColorPressed: '#738ba3',
        successColor: '#91a978',
        successColorHover: '#a3ba8d',
        successColorPressed: '#7b9363',
        warningColor: '#d0a065',
        warningColorHover: '#ddb07d',
        warningColorPressed: '#bb8951',
        errorColor: '#c97a70',
        errorColorHover: '#d88f86',
        errorColorPressed: '#b3655c',
        borderRadius: '18px',
        bodyColor: 'rgba(50, 42, 37, 0.94)',
        cardColor: 'rgba(56, 47, 42, 0.92)',
        modalColor: 'rgba(56, 47, 42, 0.96)',
        popoverColor: 'rgba(56, 47, 42, 0.96)',
        tableColor: 'rgba(56, 47, 42, 0.92)',
        actionColor: 'rgba(77, 66, 58, 0.62)',
        hoverColor: 'rgba(95, 82, 73, 0.52)',
        borderColor: 'rgba(223, 199, 168, 0.22)',
        dividerColor: 'rgba(223, 199, 168, 0.16)',
        textColorBase: '#f1e7d7',
        textColor1: '#f1e7d7',
        textColor2: '#d4c6b1',
        textColor3: '#a79886',
        placeholderColor: '#8d7f70',
        placeholderColorDisabled: '#736759',
        closeIconColor: '#d4c6b1',
        closeColorHover: 'rgba(95, 82, 73, 0.52)',
        closeColorPressed: 'rgba(95, 82, 73, 0.7)',
        inputColorDisabled: 'rgba(77, 66, 58, 0.55)',
        scrollbarColor: 'rgba(223, 199, 168, 0.2)',
        scrollbarColorHover: 'rgba(223, 199, 168, 0.36)'
      }
    }
  }

  return {
    common: {
      fontFamily:
        '"Noto Sans SC", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
      fontFamilyMono:
        '"Cascadia Code", "SFMono-Regular", Consolas, "Liberation Mono", monospace',
      primaryColor: '#768c6a',
      primaryColorHover: '#8b9f80',
      primaryColorPressed: '#627556',
      primaryColorSuppl: '#768c6a',
      infoColor: '#6f89a3',
      infoColorHover: '#829ab2',
      infoColorPressed: '#5d7690',
      successColor: '#7d9563',
      successColorHover: '#92a977',
      successColorPressed: '#6b8055',
      warningColor: '#c69054',
      warningColorHover: '#d5a46f',
      warningColorPressed: '#b27b44',
      errorColor: '#b86659',
      errorColorHover: '#c77a6f',
      errorColorPressed: '#a05549',
      borderRadius: '18px',
      bodyColor: 'rgba(252, 248, 241, 0.92)',
      cardColor: 'rgba(252, 248, 241, 0.92)',
      modalColor: 'rgba(252, 248, 241, 0.96)',
      popoverColor: 'rgba(252, 248, 241, 0.96)',
      tableColor: 'rgba(252, 248, 241, 0.92)',
      actionColor: 'rgba(244, 237, 222, 0.78)',
      hoverColor: 'rgba(235, 224, 205, 0.78)',
      borderColor: 'rgba(145, 120, 91, 0.24)',
      dividerColor: 'rgba(145, 120, 91, 0.14)',
      textColorBase: '#3d352e',
      textColor1: '#3d352e',
      textColor2: '#6e6257',
      textColor3: '#8b7b69',
      placeholderColor: '#9b8d7d',
      placeholderColorDisabled: '#b5a697',
      closeIconColor: '#6e6257',
      closeColorHover: 'rgba(235, 224, 205, 0.78)',
      closeColorPressed: 'rgba(224, 210, 187, 0.9)',
      inputColorDisabled: 'rgba(236, 228, 214, 0.72)',
      scrollbarColor: 'rgba(145, 120, 91, 0.18)',
      scrollbarColorHover: 'rgba(145, 120, 91, 0.34)'
    }
  }
}
