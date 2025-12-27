<script setup>
import { ref, inject, onMounted, nextTick } from 'vue'
import axios from "axios"
import ModalSquare from "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\components\\ModalSquare.vue";
import NutrientsContriRateGraph from '../components/NutrientsContriRateGraph.vue';
import PfcContriRateGraph from '../components/PfcContriRateGraph.vue';

const closeFullScreen = inject('closeFullScreen')
const isExist = ref(false)

const props = defineProps({
  data: Object
})

onMounted( async () => {
  const res = await axios.post(`http://localhost:50000/psqlFavorites/exist`, props.data)
  isExist.value = res.data    // 存在する→true, 存在しない→false
  // eslint-disable-next-line no-debugger
  debugger
})

// --- モーダルの表示まわり ---
const activeModal = ref(null); // 'sort' | 'filter' | 'options' | 'graph' | null      <ModalSquare :show="activeModal === '○○'"に引っかかることで任意のモーダルを呼び出す  
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

// --- SQLでfavoritesテーブルに保存するAPI ---
const save = async (data) => {
  console.log("お気に入り登録するよ");
  await axios.post(`http://localhost:50000/psqlFavorites/save`, data);
  isExist.value = true
}

</script>

<template>
  <div class="flex flex-col h-full">
    <!-- ヘッダー -->
    <header class="sticky top-0 flex items-center px-4 h-20 bg-white border-b">
      <!-- 左：戻るボタン -->
      <button @click="closeFullScreen" class="flex items-center">
        <span class="material-symbols-outlined">arrow_back_ios</span>
      </button>
      <h2 class="font-semibold">{{ props.data.menuName }}</h2>
    </header>

    <!-- メインコンテンツ -->
    <div class="flex-1 p-4 bg-warm overflow-y-auto">
      <!-- 合計金額と総カロリーの部分 -->
      <div class="bg-white rounded-lg p-4 mb-4">
        <div class="flex justify-between">
          <div class="flex items-center gap-2">
            <span class="text-sm font-medium">合計金額：</span>
            <span class="text-xl font-medium">{{ props.data.totalPrice }}円</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm font-medium">総カロリー：</span>
            <span class="text-xl font-medium">{{ props.data.totalKcal }}kcal</span>
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
          <div v-for="([type, dishName], index) in Object.entries(props.data.dishName).filter(([k, v]) => v)"
            :key="index">
            <p class="ml-2">{{ type }} ： {{ dishName }}</p>
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
          <div v-for="([ingredient, amount], index) in Object.entries(props.data.ingredients).filter(([k, v]) => v)"
            :key="index" class="flex justify-between items-start">
            <span class="text-left break-words ml-2">{{ ingredient }}</span>
            <span class="text-right break-words mr-4">{{ amount }}</span>
          </div>
          <div class="h-px bg-gray-300 my-2"></div>
          <div v-for="([ingredient, amount], index) in Object.entries(props.data.seasonings).filter(([k, v]) => v)"
            :key="index" class="flex justify-between items-start">
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
          <div v-for="([dishName, instruction], index) in Object.entries(props.data.instructions).filter(([k, v]) => v)"
            :key="index" class="flex flex-col">
            <span class="">{{ dishName }}</span>
            <span class="ml-2 mb-2 whitespace-pre-wrap">{{ instruction }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- フッター -->
    <footer class="sticky bottom-0 flex justify-between items-center h-24 gap-3 bg-white border-t">
      <div
        class="sticky flex bottom-20 left-0 right-0 pt-2 px-4 grow gap-3 bg-gradient-to-t from-white via-white/100 to-transparent">
        <!-- お気に入り登録ボタン -->
        <div v-if="!isExist">
          <div class="flex p-3 gap-4 grow rounded-full justify-center bg-orange-400">
            <span class="material-symbols-outlined text-white">favorite</span>
            <button class="text-white font-medium"
              @click = "() => {
                save(props.data);
                nextTick(() => openModal('registered'))
              }">お気に入りに登録</button>
          </div>
        </div>
        <div v-else>
          <div class="flex p-3 gap-4 grow rounded-full justify-center bg-orange-400">
            <span class="material-symbols-outlined text-white">favorite</span>
            <button class="text-white font-medium">登録済みです！</button>
          </div>
        </div>
        <!-- 栄養グラフ描画ボタン -->
        <button @click="() => {
          openModal('graph');
          nextTick(() => openGraph(props.data.nutrientsContriRate, props.data.pfcContriRate));
        }"><!-- openModalでモーダルが表示された後、openGraphでデータが書き換わりグラフ描画がされる -->
          <span class="material-symbols-outlined flex items-center justify-center rounded-full w-12 h-12 text-2xl text-white bg-orange-400">bar_chart_4_bars</span>
        </button>
        <!-- メモボタン -->
        <span class="material-symbols-outlined flex items-center justify-center rounded-full w-12 h-12 text-2xl text-white bg-orange-400">edit_note</span>
      </div>
    </footer>

    <!-- ↓モーダルウィンドウで表示するやつ -->
    <!-- お気に入り登録後のポップアップ -->
    <ModalSquare :show="activeModal === 'registered'" width="60%" @close="closeModal">
      <p>登録しました！</p>
    </ModalSquare>
    <!-- 寄与率のグラフ描画 -->
    <ModalSquare :show="activeModal === 'graph'" width="100%" height="70%" @close="closeModal">
      <div class="relative">
        <!-- 付箋風タブ切り替え -->
        <div class="absolute -top-14 flex z-10">
          <button @click="graphTab = 'nutrients'" class="px-4 py-1 rounded-t-lg"
            :class="graphTab === 'nutrients' ? 'bg-white text-orange-600 border-b-0' : 'bg-gray-200 text-gray-600'">栄養素</button>
          <button @click="graphTab = 'calories'" class="px-4 py-1 rounded-t-lg"
            :class="graphTab === 'calories' ? 'bg-white text-orange-600 border-b-0' : 'bg-gray-200 text-gray-600'">カロリー</button>
        </div>
        <div v-show="graphTab === 'nutrients'">
          <NutrientsContriRateGraph :graphData="nutrientsContriGraphData" />
        </div>
        <div v-show="graphTab === 'calories'">
          <PfcContriRateGraph :graphData="pfcContriGraphData" />
        </div>
      </div>
    </ModalSquare>
  </div>
</template>