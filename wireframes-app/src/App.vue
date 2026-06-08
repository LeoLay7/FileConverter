<template>
  <div class="app">
    <header class="topbar">
      <router-link to="/" class="logo">⚙ DataConverter</router-link>
      <nav>
        <router-link to="/">Главная</router-link>
        <router-link to="/upload">Конвертировать</router-link>
      </nav>
    </header>
    <main>
      <router-view />
    </main>
    <footer class="footer">
      <span>© 2025 DataConverter — конвертер форматов данных</span>
    </footer>
  </div>
</template>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', sans-serif; background: #f0f2f5; color: #222; }

.app { min-height: 100vh; display: flex; flex-direction: column; }

.topbar {
  display: flex; align-items: center; gap: 24px;
  padding: 14px 40px;
  background: #1a1a2e; color: #fff;
  border-bottom: 3px solid #e94560;
}
.logo {
  font-size: 1.2rem; font-weight: 700; letter-spacing: 1px;
  margin-right: auto; color: #fff; text-decoration: none;
}

nav { display: flex; gap: 8px; }
nav a {
  color: #aaa; text-decoration: none;
  padding: 6px 16px; border-radius: 6px;
  font-size: 0.9rem; transition: background 0.2s;
}
nav a:hover, nav a.router-link-active {
  background: #e94560; color: #fff;
}

main { flex: 1; padding: 40px; }

.footer {
  text-align: center; padding: 16px;
  font-size: 0.8rem; color: #aaa;
  border-top: 1px solid #dde1e7;
  background: #fff;
}

/* Shared wireframe styles */
.wf-card {
  background: #fff; border-radius: 10px;
  border: 1px solid #dde1e7; padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.wf-title { font-size: 1.1rem; font-weight: 700; margin-bottom: 16px; color: #1a1a2e; }
.wf-label { font-size: 0.75rem; color: #888; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px; }
.wf-placeholder {
  background: #f7f8fa; border: 2px dashed #ccc;
  border-radius: 8px; display: flex; align-items: center;
  justify-content: center; color: #aaa; font-size: 0.85rem;
}
.wf-btn {
  padding: 8px 20px; border-radius: 6px; border: none;
  cursor: pointer; font-size: 0.9rem; font-weight: 600;
  transition: opacity 0.2s;
}
.wf-btn:hover { opacity: 0.85; }
.wf-btn-primary { background: #e94560; color: #fff; }
.wf-btn-secondary { background: #eef0f4; color: #444; }
.wf-btn-ghost {
  background: transparent; border: 1px solid #ccc; color: #555;
}
</style>
