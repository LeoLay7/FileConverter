import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import App from './App.vue'
import HomeView from './views/HomeView.vue'
import UploadView from './views/UploadView.vue'
import SchemaView from './views/SchemaView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/upload', component: UploadView },
    { path: '/schema', component: SchemaView },
  ]
})

createApp(App).use(router).mount('#app')
