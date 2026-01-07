<script setup>
import { defineProps, computed } from 'vue'

const props = defineProps({
  data: Object
})

const listSize = computed(() =>
  props.data.length
)
</script>

<template>
  <div class="flex overflow-x-scroll snap-x gap-4 p-4">
    <div v-for="(solution, index) in props.data" :key="index" class="flex-shrink-0 w-full snap-center">
      <!-- 白色の角丸四角形 -->
      <div class="bg-white rounded-2xl shadow-md overflow-hidden flex flex-col">
        <!-- 上の緑の帯 -->
        <div class="h-2 bg-gradient-to-r from-green-500 to-green-200"></div>
        <!-- カードの中身全体 -->
        <div class="p-4">
          <!-- 合計金額と総カロリーの部分 -->
          <div class="flex justify-between text-sm">
            <div class="flex items-center gap-2">
              <span class="font-medium ">合計金額：{{ solution.totalPrice }}円</span>
            </div>
            <div class="flex items-center gap-2">
              <span class=" font-medium ">総カロリー：{{ solution.totalKcal }}kcal</span>
            </div>
          </div>
          <div class="h-px bg-gray-300 my-2"></div>

          <!-- 食材リスト -->
          <div class="my-4">
            <div class="flex items-center gap-2 mb-4 text-sm">
              <span class="material-symbols-outlined">list</span>
              <span class=" font-medium ">食材</span>
            </div>

            <!-- 食材名とグラム数 -->
            <div class="space-y-1.5 h-60 leading-5 flex-1 overflow-y-auto text-sm">
              <div v-for="(amount, name) in solution.ingredients" :key="name"
                class="flex justify-between items-start gap-4">
                <span class="text-left break-words ml-2">{{ name }}</span>
                <span class="text-right break-words ml-2">{{ amount }}</span>
              </div>
            </div>
          </div>


          <span class="text-sm font-medium ">{{ index + 1 }}/{{ listSize }}</span>
        </div>
      </div>
    </div>
  </div>
</template>