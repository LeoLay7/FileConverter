<template>
  <div class="schema-page">
    <div class="page-header">
      <h1>Структура файла</h1>
      <p class="subtitle">Просмотрите поля и настройте правила конвертации</p>
    </div>

    <!-- Toolbar -->
    <div class="schema-toolbar">
      <div class="file-badge">
        <span :class="['fmt-tag', mockFile.format.toLowerCase()]">{{ mockFile.format }}</span>
        <span class="file-name">{{ mockFile.name }}</span>
      </div>
      <div class="toolbar-right">
        <button class="wf-btn wf-btn-ghost" @click="expandAll">Развернуть всё</button>
        <button class="wf-btn wf-btn-ghost" @click="collapseAll">Свернуть всё</button>
        <router-link to="/upload">
          <button class="wf-btn wf-btn-secondary">← Загрузить другой</button>
        </router-link>
      </div>
    </div>

    <!-- Main layout -->
    <div class="schema-layout">

      <!-- Tree panel -->
      <div class="wf-card tree-panel">
        <div class="wf-title">Поля файла</div>
        <div class="legend">
          <span v-for="t in typeColors" :key="t.type" class="legend-item">
            <span :class="['dot', t.type]"></span>{{ t.label }}
          </span>
        </div>
        <SchemaTree
          :nodes="schema"
          :selected-path="selectedPath"
          @select="onSelect"
        />
      </div>

      <!-- Detail panel -->
      <div class="wf-card detail-panel">
        <div class="wf-title">Детали поля</div>
        <template v-if="selectedNode">
          <div class="detail-grid">
            <div class="detail-row">
              <span class="detail-key">Путь</span>
              <code class="detail-path">{{ selectedPath }}</code>
            </div>
            <div class="detail-row">
              <span class="detail-key">Тип данных</span>
              <span :class="['type-badge', selectedNode.type]">{{ selectedNode.type }}</span>
            </div>
            <div class="detail-row" v-if="selectedNode.value !== undefined">
              <span class="detail-key">Пример значения</span>
              <span class="sample-val">{{ selectedNode.value }}</span>
            </div>
            <div class="detail-row" v-if="selectedNode.children">
              <span class="detail-key">Вложенных полей</span>
              <span>{{ selectedNode.children.length }}</span>
            </div>
          </div>

          <div class="ops-section">
            <div class="wf-label">Операции с полем</div>
            <div class="ops-grid">
              <button class="op-btn" disabled>✏ Переименовать</button>
              <button class="op-btn" disabled>🔄 Изменить тип</button>
              <button class="op-btn" disabled>🗑 Исключить</button>
              <button class="op-btn" disabled>📦 Переместить</button>
            </div>
            <p class="ops-note">Настройка правил будет доступна на следующем шаге</p>
          </div>
        </template>
        <div v-else class="wf-placeholder" style="height: 220px">
          ← Выберите поле в дереве
        </div>
      </div>

    </div>

    <!-- Stats -->
    <div class="wf-card stats-bar">
      <div class="stat" v-for="s in stats" :key="s.label">
        <span class="stat-val">{{ s.val }}</span>
        <span class="stat-label">{{ s.label }}</span>
      </div>
    </div>

    <!-- Action -->
    <div class="wf-card action-bar">
      <span class="action-hint">Выберите поля для конвертации и перейдите к настройке правил</span>
      <button class="wf-btn wf-btn-primary" disabled>
        Настроить правила маппинга →
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import SchemaTree from '../components/SchemaTree.vue'

const mockFile = { name: 'users.json', format: 'JSON' }

const schema = ref([
  {
    key: 'users', type: 'array', path: 'users',
    children: [
      {
        key: '[0]', type: 'object', path: 'users[0]',
        children: [
          { key: 'id',     type: 'integer', path: 'users[0].id',    value: 1 },
          { key: 'name',   type: 'string',  path: 'users[0].name',  value: 'Alice' },
          { key: 'email',  type: 'string',  path: 'users[0].email', value: 'alice@example.com' },
          {
            key: 'address', type: 'object', path: 'users[0].address',
            children: [
              { key: 'city', type: 'string', path: 'users[0].address.city', value: 'Moscow' },
              { key: 'zip',  type: 'string', path: 'users[0].address.zip',  value: '101000' },
            ]
          },
          { key: 'active', type: 'boolean', path: 'users[0].active', value: true },
        ]
      }
    ]
  },
  { key: 'total', type: 'integer', path: 'total', value: 42 },
  { key: 'page',  type: 'integer', path: 'page',  value: 1 },
])

const selectedPath = ref(null)
const selectedNode = ref(null)

function findNode(nodes, path) {
  for (const n of nodes) {
    if (n.path === path) return n
    if (n.children) {
      const found = findNode(n.children, path)
      if (found) return found
    }
  }
  return null
}

function onSelect(path) {
  selectedPath.value = path
  selectedNode.value = findNode(schema.value, path)
}

function setExpanded(nodes, val) {
  nodes.forEach(n => {
    n._open = val
    if (n.children) setExpanded(n.children, val)
  })
  schema.value = [...schema.value]
}
function expandAll()   { setExpanded(schema.value, true) }
function collapseAll() { setExpanded(schema.value, false) }

const stats = [
  { val: 9,      label: 'Всего полей' },
  { val: 3,      label: 'Объектов' },
  { val: 1,      label: 'Массивов' },
  { val: 3,      label: 'Уровней вложенности' },
]

const typeColors = [
  { type: 'string',  label: 'Строка' },
  { type: 'integer', label: 'Число' },
  { type: 'boolean', label: 'Булево' },
  { type: 'object',  label: 'Объект' },
  { type: 'array',   label: 'Массив' },
]
</script>

<style scoped>
.schema-page { display: flex; flex-direction: column; gap: 20px; max-width: 1100px; margin: 0 auto; }
.page-header h1 { font-size: 1.6rem; color: #1a1a2e; }
.subtitle { color: #888; font-size: 0.9rem; margin-top: 4px; }

.schema-toolbar {
  display: flex; align-items: center; gap: 12px;
  background: #fff; padding: 12px 20px; border-radius: 10px;
  border: 1px solid #dde1e7; flex-wrap: wrap;
}
.file-badge { display: flex; align-items: center; gap: 10px; font-size: 0.9rem; font-weight: 600; }
.fmt-tag {
  padding: 3px 10px; border-radius: 4px; font-weight: 700; font-size: 0.8rem;
}
.fmt-tag.json { background: #e3f2fd; color: #1565c0; }
.fmt-tag.xml  { background: #f3e5f5; color: #6a1b9a; }
.fmt-tag.csv  { background: #e8f5e9; color: #2e7d32; }
.file-name { color: #444; }
.toolbar-right { display: flex; gap: 8px; margin-left: auto; flex-wrap: wrap; }

.schema-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.tree-panel { min-height: 400px; }

.legend {
  display: flex; flex-wrap: wrap; gap: 12px;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.legend-item { display: flex; align-items: center; gap: 5px; font-size: 0.78rem; color: #666; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot.string  { background: #4caf50; }
.dot.integer { background: #2196f3; }
.dot.boolean { background: #ff9800; }
.dot.object  { background: #9c27b0; }
.dot.array   { background: #e94560; }

.detail-grid { display: flex; flex-direction: column; gap: 14px; margin-bottom: 24px; }
.detail-row { display: flex; justify-content: space-between; align-items: center; font-size: 0.9rem; }
.detail-key { color: #888; font-size: 0.8rem; }
.detail-path { font-family: monospace; font-size: 0.82rem; color: #555; background: #f7f8fa; padding: 2px 8px; border-radius: 4px; }
.type-badge {
  padding: 2px 10px; border-radius: 4px; font-size: 0.8rem; font-weight: 700;
}
.type-badge.string  { background: #e8f5e9; color: #2e7d32; }
.type-badge.integer { background: #e3f2fd; color: #1565c0; }
.type-badge.boolean { background: #fff3e0; color: #e65100; }
.type-badge.object  { background: #f3e5f5; color: #6a1b9a; }
.type-badge.array   { background: #fce4ec; color: #880e4f; }
.sample-val { font-family: monospace; color: #555; font-size: 0.85rem; }

.ops-section { border-top: 1px solid #eee; padding-top: 16px; }
.ops-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin: 10px 0; }
.op-btn {
  padding: 9px; border-radius: 6px; border: 1px dashed #ccc;
  background: #f7f8fa; color: #bbb; font-size: 0.82rem; cursor: not-allowed;
}
.ops-note { font-size: 0.78rem; color: #bbb; font-style: italic; }

.stats-bar { display: flex; padding: 0; overflow: hidden; }
.stat {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  padding: 18px; border-right: 1px solid #eee;
}
.stat:last-child { border-right: none; }
.stat-val { font-size: 1.5rem; font-weight: 700; color: #1a1a2e; }
.stat-label { font-size: 0.75rem; color: #888; margin-top: 2px; }

.action-bar {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: 12px;
}
.action-hint { color: #888; font-size: 0.9rem; }
.wf-btn:disabled { opacity: 0.4; cursor: not-allowed; }

@media (max-width: 700px) {
  .schema-layout { grid-template-columns: 1fr; }
  .stats-bar { flex-wrap: wrap; }
  .stat { min-width: 50%; }
}
</style>
