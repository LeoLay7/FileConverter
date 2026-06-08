<template>
  <div
    class="drop-zone"
    :class="{ dragging }"
    @dragover.prevent="dragging = true"
    @dragleave="dragging = false"
    @drop.prevent="onDrop"
    @click="$refs.input.click()"
  >
    <input ref="input" type="file" accept=".json,.xml,.csv" hidden @change="onInput" />
    <div class="dz-icon">{{ dragging ? '📂' : '⬆' }}</div>
    <div class="dz-text">
      <strong>Перетащите файл сюда</strong> или кликните для выбора
    </div>
    <div class="dz-hint">Поддерживаемые форматы: JSON, XML, CSV · Макс. 10 MB</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['file-selected'])
const dragging = ref(false)

function onDrop(e) {
  dragging.value = false
  const file = e.dataTransfer.files[0]
  if (file) emit('file-selected', file)
}

function onInput(e) {
  const file = e.target.files[0]
  if (file) emit('file-selected', file)
}
</script>

<style scoped>
.drop-zone {
  border: 2px dashed #ccc; border-radius: 12px;
  padding: 48px 24px; text-align: center;
  cursor: pointer; transition: all 0.2s;
  background: #fafbfc;
}
.drop-zone:hover, .drop-zone.dragging {
  border-color: #e94560; background: #fff0f3;
}
.dz-icon { font-size: 2.5rem; margin-bottom: 12px; }
.dz-text { font-size: 0.95rem; color: #444; margin-bottom: 6px; }
.dz-hint { font-size: 0.8rem; color: #aaa; }
</style>
