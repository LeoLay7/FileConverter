<template>
  <div class="home">

    <!-- Hero -->
    <section class="hero">
      <div class="hero-text">
        <h1>Конвертируйте данные<br>без лишних усилий</h1>
        <p>Загрузите файл, настройте правила сопоставления полей и получите результат в нужном формате. Поддерживаются JSON, XML и CSV.</p>
        <router-link to="/upload">
          <button class="wf-btn wf-btn-primary hero-btn">Начать конвертацию →</button>
        </router-link>
      </div>
      <div class="hero-visual">
        <div class="format-flow">
          <div class="fmt-box json">JSON</div>
          <div class="flow-arrows">
            <span>→</span>
            <span class="flow-icon">⚙</span>
            <span>→</span>
          </div>
          <div class="fmt-stack">
            <div class="fmt-box xml">XML</div>
            <div class="fmt-box csv">CSV</div>
            <div class="fmt-box json2">JSON</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="features">
      <div class="feature-card" v-for="f in features" :key="f.title">
        <div class="feature-icon">{{ f.icon }}</div>
        <div class="feature-title">{{ f.title }}</div>
        <div class="feature-desc">{{ f.desc }}</div>
      </div>
    </section>

    <!-- How it works -->
    <div class="wf-card how-it-works">
      <div class="wf-title" style="text-align:center; font-size:1.3rem; margin-bottom:32px">
        Как это работает
      </div>
      <div class="steps">
        <div class="step" v-for="(s, i) in steps" :key="s.title">
          <div class="step-num">{{ i + 1 }}</div>
          <div class="step-icon">{{ s.icon }}</div>
          <div class="step-title">{{ s.title }}</div>
          <div class="step-desc">{{ s.desc }}</div>
          <div class="step-arrow" v-if="i < steps.length - 1">→</div>
        </div>
      </div>
    </div>

    <!-- Supported formats -->
    <div class="wf-card formats-card">
      <div class="wf-title">Поддерживаемые форматы</div>
      <div class="formats-grid">
        <div class="format-item" v-for="f in formats" :key="f.name">
          <span :class="['fmt-badge', f.cls]">{{ f.name }}</span>
          <span class="fmt-desc">{{ f.desc }}</span>
        </div>
      </div>
    </div>

    <!-- CTA -->
    <div class="cta-banner">
      <span>Готовы попробовать?</span>
      <router-link to="/upload">
        <button class="wf-btn wf-btn-primary">Загрузить файл</button>
      </router-link>
    </div>

  </div>
</template>

<script setup>
const features = [
  { icon: '🔄', title: 'Гибкое сопоставление', desc: 'Переименовывайте поля, меняйте вложенность и типы данных через удобный интерфейс.' },
  { icon: '🌳', title: 'Визуализация структуры', desc: 'Дерево полей наглядно показывает структуру вашего файла до и после конвертации.' },
  { icon: '⚡', title: 'Быстрый результат', desc: 'Загрузите файл, настройте правила и скачайте результат за несколько секунд.' },
  { icon: '🛡', title: 'Валидация данных', desc: 'Система проверяет корректность файла и предупреждает об ошибках до конвертации.' },
]

const steps = [
  { icon: '📂', title: 'Загрузите файл', desc: 'JSON, XML или CSV — перетащите или выберите вручную' },
  { icon: '🌳', title: 'Изучите структуру', desc: 'Просмотрите дерево полей вашего файла' },
  { icon: '✏', title: 'Настройте правила', desc: 'Сопоставьте поля, переименуйте, удалите лишнее' },
  { icon: '💾', title: 'Скачайте результат', desc: 'Получите файл в нужном формате' },
]

const formats = [
  { name: 'JSON', cls: 'json', desc: 'JavaScript Object Notation — универсальный формат обмена данными' },
  { name: 'XML',  cls: 'xml',  desc: 'Extensible Markup Language — стандарт для структурированных документов' },
  { name: 'CSV',  cls: 'csv',  desc: 'Comma-Separated Values — табличные данные, совместимые с Excel' },
]
</script>

<style scoped>
.home { display: flex; flex-direction: column; gap: 28px; max-width: 1100px; margin: 0 auto; }

/* Hero */
.hero {
  display: flex; align-items: center; gap: 40px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px; padding: 48px 48px;
  color: #fff; overflow: hidden;
}
.hero-text { flex: 1; }
.hero-text h1 { font-size: 2rem; line-height: 1.3; margin-bottom: 16px; }
.hero-text p { color: #aaa; font-size: 1rem; line-height: 1.6; margin-bottom: 28px; max-width: 420px; }
.hero-btn { padding: 12px 28px; font-size: 1rem; }

.hero-visual { flex-shrink: 0; }
.format-flow { display: flex; align-items: center; gap: 16px; }
.fmt-box {
  padding: 12px 20px; border-radius: 10px;
  font-weight: 800; font-size: 1rem; letter-spacing: 1px;
}
.fmt-box.json  { background: #e3f2fd; color: #1565c0; }
.fmt-box.json2 { background: #e3f2fd; color: #1565c0; }
.fmt-box.xml   { background: #f3e5f5; color: #6a1b9a; }
.fmt-box.csv   { background: #e8f5e9; color: #2e7d32; }
.flow-arrows { display: flex; align-items: center; gap: 6px; color: #aaa; font-size: 1.2rem; }
.flow-icon { font-size: 1.6rem; }
.fmt-stack { display: flex; flex-direction: column; gap: 8px; }

/* Features */
.features { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.feature-card {
  background: #fff; border-radius: 10px; padding: 24px 20px;
  border: 1px solid #dde1e7; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.feature-icon { font-size: 2rem; margin-bottom: 12px; }
.feature-title { font-weight: 700; font-size: 0.95rem; margin-bottom: 8px; color: #1a1a2e; }
.feature-desc { font-size: 0.82rem; color: #888; line-height: 1.5; }

/* Steps */
.steps { display: flex; align-items: flex-start; gap: 0; position: relative; }
.step {
  flex: 1; text-align: center; padding: 0 16px;
  position: relative;
}
.step-num {
  width: 36px; height: 36px; border-radius: 50%;
  background: #e94560; color: #fff;
  font-weight: 700; font-size: 1rem;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 12px;
}
.step-icon { font-size: 1.6rem; margin-bottom: 8px; }
.step-title { font-weight: 700; font-size: 0.9rem; margin-bottom: 6px; color: #1a1a2e; }
.step-desc { font-size: 0.8rem; color: #888; line-height: 1.4; }
.step-arrow {
  position: absolute; right: -12px; top: 10px;
  font-size: 1.2rem; color: #ccc;
}

/* Formats */
.formats-grid { display: flex; flex-direction: column; gap: 14px; }
.format-item { display: flex; align-items: center; gap: 16px; font-size: 0.9rem; }
.fmt-badge {
  padding: 4px 14px; border-radius: 6px;
  font-weight: 800; font-size: 0.85rem; min-width: 56px; text-align: center;
}
.fmt-badge.json { background: #e3f2fd; color: #1565c0; }
.fmt-badge.xml  { background: #f3e5f5; color: #6a1b9a; }
.fmt-badge.csv  { background: #e8f5e9; color: #2e7d32; }
.fmt-desc { color: #666; }

/* CTA */
.cta-banner {
  background: #e94560; border-radius: 12px;
  padding: 28px 40px; display: flex;
  align-items: center; justify-content: space-between;
  color: #fff; font-size: 1.2rem; font-weight: 700;
}
.cta-banner a { text-decoration: none; }
.cta-banner .wf-btn-primary {
  background: #fff; color: #e94560;
  padding: 10px 28px; font-size: 0.95rem;
}

@media (max-width: 900px) {
  .features { grid-template-columns: 1fr 1fr; }
  .hero { flex-direction: column; padding: 32px 24px; }
  .steps { flex-wrap: wrap; }
  .step { min-width: 45%; margin-bottom: 24px; }
  .step-arrow { display: none; }
}
@media (max-width: 600px) {
  .features { grid-template-columns: 1fr; }
  main { padding: 20px; }
}
</style>
