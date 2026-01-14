<script setup>
import { ref, defineProps, computed, nextTick } from 'vue'

import ModalSquare from "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\components\\ModalSquare.vue";
import NutrientsContriRateGraph from '../components/NutrientsContriRateGraph.vue';
import PfcContriRateGraph from '../components/PfcContriRateGraph.vue';
import PriceBreakdown from '../components/PriceBreakdown.vue';

const props = defineProps({
  data: Object
})

const priceBreakdown = ref({});
const ingredients =  ref({});
const listSize = computed(() =>
  props.data.length
)

// --- モーダルの表示まわり ---
const activeModal = ref(null); //   <ModalSquare :show="activeModal === '○○'"に引っかかることで任意のモーダルを呼び出す  
const openModal = (name) => {
  activeModal.value = name;
};
const closeModal = () => {
  activeModal.value = null;         //<ModalSquare :show="activeModal === '○○'" のいずれにも引っかからなくなる
};

// --- グラフ描画まわり ---
const graphTab = ref('nutrients');
const nutrientsContriGraphData = ref([]);
const pfcContriGraphData = ref([]);
function openGraph(nutrientsContriRate, pfcContriRate) {
  nutrientsContriGraphData.value = null;     // 一度空にする
  pfcContriGraphData.value = null;
  nextTick(() => {
    nutrientsContriGraphData.value = nutrientsContriRate;   //ここでのデータ変更がContriGraph内でwatchされ、グラフの描画が始まる
    pfcContriGraphData.value = pfcContriRate;
  })
}
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

          <!-- カードのフッター -->
          <div class="flex justify-between items-center">
            <span class="text-sm font-medium">{{ index + 1 }}/{{ listSize }}</span>
            <div class="flex gap-4">
              <button @click="() => {
                priceBreakdown = solution.priceBreakdown;
                ingredients = solution.ingredients;
                openModal('priceBreakdown');
              }">
                <span class="material-symbols-outlined text-green-600 text-2xl">currency_yen</span>
              </button>
              <button @click="() => {
                openModal('contriGraph');
                nextTick(() => openGraph(solution.nutrientsContriRate, solution.pfcContriRate));
              }">
              <span class="material-symbols-outlined text-green-600 text-2xl">bar_chart_4_bars</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 価格の内訳を表示 -->
    <ModalSquare :show="activeModal === 'priceBreakdown'" width="90%" @close="closeModal">
      <PriceBreakdown :ingredients="ingredients" :priceBreakdown="priceBreakdown"/>
    </ModalSquare>

    <!-- 寄与率のグラフ描画 -->
    <ModalSquare :show="activeModal === 'contriGraph'" width="100%" height="70%" @close="closeModal">
      <div class="relative">
        <!-- 付箋風タブ切り替え -->
        <div class="absolute -top-14 flex z-10">
          <button
            @click="graphTab = 'nutrients'"
            class="px-4 py-1 rounded-t-lg"
            :class="graphTab === 'nutrients' ? 'bg-white text-green-600 border-b-0' : 'bg-gray-200 text-gray-600'"
          >栄養素</button>
          <button
            @click="graphTab = 'calories'"
            class="px-4 py-1 rounded-t-lg"
            :class="graphTab === 'calories' ? 'bg-white text-green-600 border-b-0' : 'bg-gray-200 text-gray-600'"
          >カロリー</button>
        </div>
        <div v-show="graphTab === 'nutrients'"><NutrientsContriRateGraph :graphData="nutrientsContriGraphData"/></div>
        <div v-show="graphTab === 'calories'"><PfcContriRateGraph :graphData="pfcContriGraphData"/></div>
      </div>
    </ModalSquare>
  </div>
</template>