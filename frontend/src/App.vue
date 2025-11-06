<script setup>
import { ref, computed } from 'vue'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import DataView from './views/DataView.vue'
import MealView from './views/MealView.vue'
import HistoryView from './views/HistoryView.vue'

const activeTab = ref('data')  // ← 状態は親に持たせる

// タブに応じて表示するコンポーネントを返す computed
const activeTabComponent = computed(() => {
  switch (activeTab.value) {
    case 'data': return DataView
    case 'meal': return MealView
    case 'history': return HistoryView
    default: return MealView
  }
})

</script>

<!-- frontend/src/App.vue -->
<template>
  <div id="app" class="flex flex-col min-h-screen">
    <AppHeader />
    <main class="flex flex-1">
      <!-- メインコンテンツのコンポーネントは破棄しない -->
      <keep-alive>
        <component :is="activeTabComponent" />
      </keep-alive>
    </main>
    <AppFooter v-model="activeTab" />
  </div>
</template>
