<script setup>
import { ref } from 'vue'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'

const activeTab = ref('meal')  // ← 状態は親に持たせる
</script>

<!-- frontend/src/App.vue -->
<template>
  <div id="app" class="flex flex-col min-h-screen">
    <AppHeader />
    <main class="flex-1 text-center bg-gray-100">
      <div v-if="activeTab === 'data'">データタブの表示中</div>
      <div v-else-if="activeTab === 'meal'">献立タブの表示中</div>
      <div v-else-if="activeTab === 'history'">履歴タブの表示中</div>
    </main>
    <AppFooter v-model="activeTab" />
  </div>
</template>
