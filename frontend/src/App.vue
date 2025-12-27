<script setup>
import { ref, computed, provide } from 'vue'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import DataView from './views/DataView.vue'
import MealView from './views/MealView.vue'
import MealView_discard from './views/MealView_discard.vue'
//import FavoriteView from './views/FavoriteView.vue'

import MealDetail from './components/MealDetail.vue'

const activeTab = ref('data')  // ← 状態は親に持たせる
// タブに応じて表示するコンポーネントを返す computed
const activeTabComponent = computed(() => {
  switch (activeTab.value) {
    case 'data': return DataView
    case 'meal': return MealView
    case 'favorite': return MealView_discard
    default: return MealView
  }
})

// フルスクリーンで表示するDOMとそのフラグ、渡したいデータを管理
const fullScreenName = ref(null);  // 'mealDetail' など
const fullScreenData = ref(null);
const fullScreenVisible = ref(false);
const fullScreenComponent = computed(() => {
  switch (fullScreenName.value) {
    case 'mealDetail':
      return MealDetail
    default:
      return null
  }
})
// 子コンポーネントから呼び出される関数(開く・閉じる)
function openFullScreen(name, data) {
  fullScreenName.value = name
  fullScreenData.value = data
  fullScreenVisible.value = true
}
function closeFullScreen() {
  fullScreenVisible.value = false
}
provide('openFullScreen', openFullScreen)
provide('closeFullScreen', closeFullScreen)

</script>

<!-- frontend/src/App.vue -->
<template>
  <div id="app" class="flex flex-col min-h-screen">
    <!-- ヘッダーとフッターに挟まれたコンポーネントを適切に切り替える -->
    <AppHeader />
    <main class="flex flex-1">
      <keep-alive>
        <component :is="activeTabComponent"/>
      </keep-alive>
    </main>
    <AppFooter v-model="activeTab" />

    <!-- フルスクリーンで表示するコンポーネント -->
    <transition name="slide-left">
      <div
        v-if="fullScreenVisible"
        class="fixed inset-0 z-50 bg-white"
      >
        <component 
          :is="fullScreenComponent"
          :data="fullScreenData"/>
      </div>
    </transition>
  </div>
</template>
