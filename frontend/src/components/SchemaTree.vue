<template>
  <ul class="tree">
    <li v-for="node in nodes" :key="node.path">
      <div
        :class="['tree-row', { selected: selectedPath === node.path }]"
        :style="{ paddingLeft: depth * 16 + 'px' }"
        @click="toggle(node)"
      >
        <span class="toggle-icon" v-if="node.children">
          {{ node._open ? '▾' : '▸' }}
        </span>
        <span class="toggle-icon leaf" v-else>·</span>
        <span :class="['type-dot', node.type]"></span>
        <span class="node-key">{{ node.key }}</span>
        <span class="node-type">{{ node.type }}</span>
        <span class="node-val" v-if="node.value !== undefined">{{ node.value }}</span>
      </div>
      <SchemaTree
        v-if="node.children && node._open"
        :nodes="node.children"
        :depth="depth + 1"
        :selected-path="selectedPath"
        @select="$emit('select', $event)"
      />
    </li>
  </ul>
</template>

<script setup>
const props = defineProps({
  nodes: Array,
  depth: { type: Number, default: 0 },
  selectedPath: String,
})
const emit = defineEmits(['select'])

function toggle(node) {
  if (node.children) {
    node._open = !node._open
  }
  emit('select', node.path)
}
</script>

<style scoped>
.tree { list-style: none; padding: 0; margin: 0; }

.tree-row {
  display: flex; align-items: center; gap: 6px;
  padding: 5px 8px; border-radius: 6px; cursor: pointer;
  font-size: 0.88rem; transition: background 0.1s;
}
.tree-row:hover { background: #f0f2f5; }
.tree-row.selected { background: #fff0f3; }

.toggle-icon { width: 14px; text-align: center; color: #888; font-size: 0.8rem; }
.toggle-icon.leaf { color: #ccc; }

.type-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.type-dot.string  { background: #4caf50; }
.type-dot.integer { background: #2196f3; }
.type-dot.boolean { background: #ff9800; }
.type-dot.object  { background: #9c27b0; }
.type-dot.array   { background: #e94560; }

.node-key { font-weight: 600; color: #1a1a2e; }
.node-type { color: #aaa; font-size: 0.75rem; margin-left: 4px; }
.node-val {
  margin-left: auto; color: #888; font-family: monospace;
  font-size: 0.78rem; max-width: 120px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
</style>
