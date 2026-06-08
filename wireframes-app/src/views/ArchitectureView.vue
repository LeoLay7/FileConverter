<template>
  <div class="arch-page">
    <div class="page-header">
      <h1>Архитектура системы</h1>
      <p class="subtitle">Спринт 1 — Wireframe схемы компонентов и потока данных</p>
    </div>

    <!-- Architecture Diagram -->
    <div class="wf-card arch-diagram">
      <div class="wf-title">Поток данных (ETL Pipeline)</div>
      <div class="pipeline">
        <div class="pipe-block source">
          <div class="pipe-icon">📂</div>
          <div class="pipe-label">Input File</div>
          <div class="pipe-formats">JSON / XML / CSV</div>
        </div>
        <div class="pipe-arrow">→</div>
        <div class="pipe-block parser">
          <div class="pipe-icon">🔍</div>
          <div class="pipe-label">Parser</div>
          <div class="pipe-formats">Java Backend</div>
        </div>
        <div class="pipe-arrow">→</div>
        <div class="pipe-block model">
          <div class="pipe-icon">🌳</div>
          <div class="pipe-label">Universal Model</div>
          <div class="pipe-formats">Internal Tree</div>
        </div>
        <div class="pipe-arrow">→</div>
        <div class="pipe-block engine">
          <div class="pipe-icon">⚙</div>
          <div class="pipe-label">Transform Engine</div>
          <div class="pipe-formats">Rules Applied</div>
        </div>
        <div class="pipe-arrow">→</div>
        <div class="pipe-block output">
          <div class="pipe-icon">💾</div>
          <div class="pipe-label">Output File</div>
          <div class="pipe-formats">JSON / XML / CSV</div>
        </div>
      </div>
    </div>

    <!-- Tech Stack -->
    <div class="two-col">
      <div class="wf-card">
        <div class="wf-title">Frontend стек</div>
        <ul class="stack-list">
          <li><span class="tag vue">Vue 3</span> SPA фреймворк</li>
          <li><span class="tag">Vue Router</span> Навигация</li>
          <li><span class="tag">Pinia</span> Стейт-менеджмент</li>
          <li><span class="tag">Axios</span> HTTP-клиент → Java API</li>
          <li><span class="tag">Vue Draggable</span> Drag-and-Drop маппинг</li>
        </ul>
      </div>
      <div class="wf-card">
        <div class="wf-title">Backend стек (Java)</div>
        <ul class="stack-list">
          <li><span class="tag java">Spring Boot</span> REST API</li>
          <li><span class="tag java">Jackson</span> JSON парсер</li>
          <li><span class="tag java">JAXB</span> XML парсер</li>
          <li><span class="tag java">OpenCSV</span> CSV парсер</li>
          <li><span class="tag java">Swagger</span> API документация</li>
        </ul>
      </div>
    </div>

    <!-- API Contract -->
    <div class="wf-card">
      <div class="wf-title">API контракт (Frontend ↔ Java Backend)</div>
      <div class="api-table">
        <div class="api-row header">
          <span>Метод</span><span>Endpoint</span><span>Описание</span><span>Ответ</span>
        </div>
        <div class="api-row" v-for="ep in endpoints" :key="ep.path">
          <span :class="['method', ep.method.toLowerCase()]">{{ ep.method }}</span>
          <span class="path">{{ ep.path }}</span>
          <span>{{ ep.desc }}</span>
          <span class="resp">{{ ep.resp }}</span>
        </div>
      </div>
    </div>

    <!-- Rules Model -->
    <div class="wf-card">
      <div class="wf-title">Модель правил конвертации (JSON-схема)</div>
      <pre class="code-block">{{ rulesExample }}</pre>
    </div>

    <!-- Wireframe Navigation -->
    <div class="wf-card nav-card">
      <div class="wf-title">Wireframes интерфейса</div>
      <p style="color:#888; margin-bottom:16px; font-size:0.9rem">
        Переходите к следующим экранам для просмотра wireframes Спринта 2
      </p>
      <div style="display:flex; gap:12px">
        <router-link to="/upload">
          <button class="wf-btn wf-btn-primary">→ Загрузка файла</button>
        </router-link>
        <router-link to="/schema">
          <button class="wf-btn wf-btn-secondary">→ Схема данных</button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
const endpoints = [
  { method: 'POST', path: '/api/files/upload', desc: 'Загрузить файл', resp: '{ fileId, schema }' },
  { method: 'GET',  path: '/api/files/{id}/schema', desc: 'Получить схему', resp: 'SchemaNode[]' },
  { method: 'POST', path: '/api/convert', desc: 'Конвертировать с правилами', resp: 'File (blob)' },
  { method: 'GET',  path: '/api/formats', desc: 'Поддерживаемые форматы', resp: 'string[]' },
]

const rulesExample = `{
  "sourceFormat": "json",
  "targetFormat": "xml",
  "mappings": [
    { "from": "user.name",  "to": "person.fullName", "type": "rename" },
    { "from": "user.age",   "to": "person.age",      "type": "cast", "targetType": "integer" },
    { "from": "user.email", "to": null,               "type": "delete" }
  ]
}`
</script>

<style scoped>
.arch-page { display: flex; flex-direction: column; gap: 20px; }
.page-header { margin-bottom: 4px; }
.page-header h1 { font-size: 1.6rem; color: #1a1a2e; }
.subtitle { color: #888; font-size: 0.9rem; margin-top: 4px; }

.arch-diagram { overflow-x: auto; }
.pipeline {
  display: flex; align-items: center; gap: 8px;
  min-width: 600px; padding: 8px 0;
}
.pipe-block {
  flex: 1; text-align: center; padding: 16px 8px;
  border-radius: 10px; border: 2px solid #dde1e7;
  background: #f7f8fa;
}
.pipe-block.source  { border-color: #4caf50; background: #f0fff4; }
.pipe-block.parser  { border-color: #2196f3; background: #f0f7ff; }
.pipe-block.model   { border-color: #9c27b0; background: #fdf0ff; }
.pipe-block.engine  { border-color: #ff9800; background: #fff8f0; }
.pipe-block.output  { border-color: #e94560; background: #fff0f3; }
.pipe-icon { font-size: 1.6rem; }
.pipe-label { font-weight: 700; font-size: 0.85rem; margin: 4px 0 2px; }
.pipe-formats { font-size: 0.75rem; color: #888; }
.pipe-arrow { font-size: 1.4rem; color: #bbb; flex-shrink: 0; }

.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

.stack-list { list-style: none; display: flex; flex-direction: column; gap: 10px; }
.stack-list li { display: flex; align-items: center; gap: 10px; font-size: 0.9rem; }
.tag {
  padding: 3px 10px; border-radius: 4px; font-size: 0.75rem;
  font-weight: 700; background: #eef0f4; color: #444; white-space: nowrap;
}
.tag.vue  { background: #e8f5e9; color: #2e7d32; }
.tag.java { background: #fff3e0; color: #e65100; }

.api-table { display: flex; flex-direction: column; gap: 2px; font-size: 0.85rem; }
.api-row { display: grid; grid-template-columns: 70px 220px 1fr 140px; gap: 12px; padding: 8px 12px; border-radius: 6px; }
.api-row.header { background: #f0f2f5; font-weight: 700; font-size: 0.75rem; color: #888; }
.api-row:not(.header):hover { background: #f7f8fa; }
.method { font-weight: 700; font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; text-align: center; align-self: center; }
.method.post { background: #e3f2fd; color: #1565c0; }
.method.get  { background: #e8f5e9; color: #2e7d32; }
.path { font-family: monospace; color: #555; }
.resp { font-family: monospace; color: #888; font-size: 0.8rem; }

.code-block {
  background: #1a1a2e; color: #a8ff78;
  padding: 16px; border-radius: 8px;
  font-size: 0.85rem; line-height: 1.6;
  overflow-x: auto;
}

.nav-card a { text-decoration: none; }

@media (max-width: 700px) {
  .two-col { grid-template-columns: 1fr; }
  .api-row { grid-template-columns: 60px 1fr; }
  .api-row span:nth-child(3), .api-row span:nth-child(4) { display: none; }
}
</style>
