<script setup>
import { inject, onMounted } from 'vue'

const closeFullScreen = inject('closeFullScreen')

const props = defineProps({
  data: Object
})

onMounted(() => {
  // eslint-disable-next-line no-debugger
  debugger
})
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- ヘッダー -->
    <header class="sticky top-0 flex items-center px-4 h-20 bg-white border-b">
      <!-- 左：戻るボタン -->
      <button @click="closeFullScreen" class="flex items-center">
        <span class="material-symbols-outlined">arrow_back_ios</span>
      </button>
      <h2 class="font-semibold">{{ props.data.menu_name }}</h2>
    </header>

    <!-- メインコンテンツ -->
    <div class="flex-1 p-4 bg-warm overflow-y-auto">
      <!-- 合計金額と総カロリーの部分 -->
      <div class="bg-white rounded-lg p-4 mb-4">
        <div class="flex justify-between">
          <div class="flex items-center gap-2">
            <span class="text-sm font-medium">合計金額：</span>
            <span class="text-xl font-medium">{{ props.data.total_price }}円</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm font-medium">総カロリー：</span>
            <span class="text-xl font-medium">{{ props.data.total_calorie }}kcal</span>
          </div>
        </div>
        <!-- 注意書き-->
        <div class="h-px bg-gray-300 my-2"></div>
        <p class="text-gray-600 text-xs">※食材のみの目安金額、調理前の合計カロリーです</p>
      </div>

      <!-- 献立表 -->
       <div class="bg-white rounded-lg p-4 mb-4">
        <!-- 見出し -->
        <div class="flex items-center gap-2 mb-4">
          <span class="material-symbols-outlined">dinner_dining</span>
          <span class="text-sm font-medium">献立表</span>
        </div>
        <!-- 主食：○○ -->
        <div class="space-y-1.5 text-sm leading-5">
          <div v-for="([type, dish_name], index) in Object.entries(props.data.dish_name).filter(([k,v]) => v)"
            :key="index">
            <p class="ml-2">{{ type }} ： {{ dish_name }}</p>
          </div>
        </div>
       </div>

      <!-- 食材リスト -->
      <div class="bg-white rounded-lg p-4 mb-4">
        <!-- 見出し -->
        <div class="flex items-center gap-2 mb-4">
          <span class="material-symbols-outlined">list</span>
          <span class="text-sm font-medium">使用する食材</span>
        </div>
        <!-- ○○：～g -->
        <div class="space-y-1.5 text-sm leading-5">
          <div v-for="([ingredient, amount], index) in Object.entries(props.data.ingredients).filter(([k,v]) => v)"
            :key="index"
            class="flex justify-between items-start">
            <span class="text-left break-words ml-2">{{ ingredient }}</span>
            <span class="text-right break-words mr-4">{{ amount }}</span>
          </div>
        </div>
      </div>

      <!-- 調理手順 -->
      <div class="bg-white rounded-lg p-4 mb-4">
        <!-- 見出し -->
        <div class="flex items-center gap-2 mb-4">
          <span class="material-symbols-outlined">flowsheet</span>
          <span class="text-sm font-medium">調理手順</span>
        </div>
        <!-- 【○○】：～～ -->
        <div class="space-y-1.5 text-sm leading-5">
          <div v-for="([dish_name, instruction], index) in Object.entries(props.data.instructions).filter(([k,v]) => v)"
            :key="index"
            class="flex flex-col">
            <span class="">{{ dish_name }}</span>
            <span class="ml-2 mb-2">{{ instruction }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- フッター -->
    <footer class="sticky bottom-0 flex justify-between items-center h-24 gap-3 bg-white border-t">
      <div class="sticky flex bottom-20 left-0 right-0 pt-2 px-4 grow gap-3 bg-gradient-to-t from-white via-white/100 to-transparent">
        <!-- お気に入り登録ボタン -->
        <div class="flex p-3 gap-4 grow rounded-full justify-center bg-orange-400">
          <span class="material-symbols-outlined text-white">favorite</span>
          <button class="text-white font-medium">お気に入りに登録</button>
        </div>
        <!-- 栄養グラフ描画ボタン -->
        <span class="material-symbols-outlined flex items-center justify-center rounded-full w-12 h-12 text-2xl text-white bg-orange-400">bar_chart_4_bars</span>
        <!-- メモボタン -->
        <span class="material-symbols-outlined flex items-center justify-center rounded-full w-12 h-12 text-2xl text-white bg-orange-400">edit_note</span>
      </div>
    </footer>
  </div>
</template>