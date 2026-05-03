<template>
  <div class="upload-page">
    <div class="page-header">
      <h1>Конвертация файла</h1>
      <p class="subtitle">Загрузите исходный файл и выберите целевой формат</p>
    </div>

    <div class="upload-layout">

      <!-- Upload Zone -->
      <div class="wf-card upload-card">
        <div class="wf-title">Шаг 1 — Выберите файл</div>
        <FileUpload @file-selected="onFileSelected" />
      </div>

      <!-- Format selector -->
      <div class="wf-card">
        <div class="wf-title">Шаг 2 — Целевой формат</div>
        <div class="wf-label">Конвертировать в:</div>
        <div class="format-grid">
          <button
            v-for="fmt in formats"
            :key="fmt.name"
            :class="['fmt-btn', { active: targetFormat === fmt.name }]"
            @click="targetFormat = fmt.name"
          >
            <span class="fmt-icon">{{ fmt.icon }}</span>
            <span>{{ fmt.name }}</span>
            <span class="fmt-hint">{{ fmt.hint }}</span>
          </button>
        </div>
      </div>

      <!-- File info -->
      <div class="wf-card" v-if="selectedFile">
        <div class="wf-title">Информация о файле</div>
        <div class="file-info">
          <div class="info-row">
            <span class="info-key">Имя файла</span>
            <span>{{ selectedFile.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-key">Размер</span>
            <span>{{ (selectedFile.size / 1024).toFixed(1) }} KB</span>
          </div>
          <div class="info-row">
            <span class="info-key">Формат</span>
            <span :class="['tag-format', detectedFormat.toLowerCase()]">{{ detectedFormat }}</span>
          </div>
          <div class="info-row">
            <span class="info-key">Статус</span>
            <span :class="['status', uploadStatus]">{{ statusLabel }}</span>
          </div>
        </div>
      </div>

      <!-- Validation error -->
      <div class="wf-card error-card" v-if="showErrors">
        <div class="wf-title">⚠ Ошибка</div>
        <ul class="error-list">
          <li v-for="e in errors" :key="e">{{ e }}</li>
        </ul>
      </div>

      <!-- Actions -->
      <div class="wf-card actions-card">
        <button
          class="wf-btn wf-btn-primary"
          :disabled="!selectedFile || !targetFormat"
          @click="handleUpload"
        >
          {{ uploadStatus === 'uploading' ? 'Загрузка...' : 'Далее — настроить поля →' }}
        </button>
        <button class="wf-btn wf-btn-ghost" @click="reset" v-if="selectedFile">
          Сбросить
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import FileUpload from '../components/FileUpload.vue'

const router = useRouter()
const selectedFile = ref(null)
const targetFormat = ref('')
const uploadStatus = ref('idle')
const showErrors = ref(false)
const errors = ref([])

const formats = [
  { name: 'JSON', icon: '{ }', hint: 'Универсальный' },
  { name: 'XML',  icon: '< >', hint: 'Разметка' },
  { name: 'CSV',  icon: '⊞',  hint: 'Таблица' },
]

const detectedFormat = computed(() => {
  if (!selectedFile.value) return ''
  const ext = selectedFile.value.name.split('.').pop().toUpperCase()
  return ['JSON', 'XML', 'CSV'].includes(ext) ? ext : 'Неизвестный'
})

const statusLabel = computed(() => ({
  idle:      '⏳ Ожидание',
  uploading: '🔄 Обработка...',
  success:   '✅ Готово',
  error:     '❌ Ошибка',
}[uploadStatus.value]))

function onFileSelected(file) {
  selectedFile.value = file
  uploadStatus.value = 'idle'
  showErrors.value = false
  errors.value = []
}

function handleUpload() {
  if (!selectedFile.value || !targetFormat.value) return
  uploadStatus.value = 'uploading'
  setTimeout(() => {
    if (detectedFormat.value === 'Неизвестный') {
      uploadStatus.value = 'error'
      showErrors.value = true
      errors.value = ['Неподдерживаемый формат. Используйте файлы JSON, XML или CSV.']
    } else {
      uploadStatus.value = 'success'
      router.push('/schema')
    }
  }, 800)
}

function reset() {
  selectedFile.value = null
  targetFormat.value = ''
  uploadStatus.value = 'idle'
  showErrors.value = false
  errors.value = []
}
</script>

<style scoped>
.upload-page { display: flex; flex-direction: column; gap: 20px; max-width: 860px; margin: 0 auto; }
.page-header h1 { font-size: 1.6rem; color: #1a1a2e; }
.subtitle { color: #888; font-size: 0.9rem; margin-top: 4px; }

.upload-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.upload-card  { grid-column: 1 / -1; }
.actions-card { grid-column: 1 / -1; display: flex; gap: 12px; align-items: center; }

.format-grid { display: flex; gap: 10px; margin-top: 8px; }
.fmt-btn {
  flex: 1; padding: 14px 8px; border-radius: 10px;
  border: 2px solid #dde1e7; background: #f7f8fa;
  font-weight: 700; font-size: 0.9rem; cursor: pointer;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  transition: all 0.15s;
}
.fmt-btn:hover { border-color: #e94560; }
.fmt-btn.active { border-color: #e94560; background: #fff0f3; color: #e94560; }
.fmt-icon { font-size: 1.2rem; font-family: monospace; }
.fmt-hint { font-size: 0.72rem; color: #aaa; font-weight: 400; }
.fmt-btn.active .fmt-hint { color: #e9456099; }

.file-info { display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; justify-content: space-between; align-items: center; font-size: 0.9rem; }
.info-key { color: #888; font-size: 0.8rem; }
.tag-format {
  padding: 2px 10px; border-radius: 4px; font-weight: 700; font-size: 0.8rem;
}
.tag-format.json { background: #e3f2fd; color: #1565c0; }
.tag-format.xml  { background: #f3e5f5; color: #6a1b9a; }
.tag-format.csv  { background: #e8f5e9; color: #2e7d32; }
.tag-format.неизвестный { background: #fce4ec; color: #880e4f; }

.status { font-weight: 600; font-size: 0.85rem; }
.status.success   { color: #2e7d32; }
.status.error     { color: #c62828; }
.status.uploading { color: #e65100; }

.error-card { border: 1px solid #ffcdd2; background: #fff5f5; grid-column: 1 / -1; }
.error-list { padding-left: 20px; color: #c62828; font-size: 0.9rem; line-height: 1.8; }

.wf-btn:disabled { opacity: 0.4; cursor: not-allowed; }

@media (max-width: 600px) {
  .upload-layout { grid-template-columns: 1fr; }
}
</style>
