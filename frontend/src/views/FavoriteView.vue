<script setup>
import { ref, onMounted, inject, nextTick } from 'vue';
import axios from "axios";

import ModalSquare from "C:\\Users\\81809\\Desktop\\demo\\frontend\\src\\components\\ModalSquare.vue";

const favoritesList = ref([]);
const findAll = async () => {
  const findRes = await axios.get('http://localhost:50000/psqlFavorites/findAll');
  favoritesList.value = findRes.data;
}

onMounted(async () => {
  await findAll();
})

const deleteMenuName = ref(null)
const setDeleteMenuName = (menuName) => {
  deleteMenuName.value = menuName
}

const openFullScreen = inject('openFullScreen')

// --- モーダルの表示まわり ---
const activeModal = ref(null); // 'beforeDelete' | 'afterDelete' | null      <ModalSquare :show="activeModal === '○○'"に引っかかることで任意のモーダルを呼び出す  
const openModal = (name) => {
  activeModal.value = name;
};
const closeModal = () => {
  activeModal.value = null;         //<ModalSquare :show="activeModal === '○○'" のいずれにも引っかからなくなる
};
</script>

<template>
  <div class="w-full px-4 pt-12 sm:px-5 flex flex-col">
    <!-- ヘッダー -->
    <h2 class="pb-1 font-semibold">
      お気に入り献立リスト
    </h2>
    <div class="pb-6 text-sm text-gray-600">
      登録済みのメニューの確認と、削除ができます
    </div>
    <!-- 献立リスト -->
    <div v-for="(favorite, idx) in favoritesList" :key="idx"
      class="flex mb-4 shadow-md border rounded-lg text-sm overflow-hidden">
      <button class="flex-1 p-4 hover:bg-green-50 active:bg-green-100" @click="openFullScreen(
        'mealDetail', favorite)">
        <div class="flex items-start gap-2">
          <span
            class="material-symbols-outlined bg-clip-text bg-gradient-to-r from-green-500 to-lime-500 text-transparent">wand_stars</span>
          <div class="font-semibold text-left">{{ favorite.menuName }}</div>
          <span class="material-symbols-outlined text-sm ml-auto">arrow_forward_ios</span>
        </div>
        <div class="flex items-start gap-6 border-t border-gray-300 mt-2 pt-2">
          <div class="px-1.5 py-0.5 font-bold">
            {{ favorite.totalPrice }}円
          </div>
          <div class="px-1.5 py-0.5 font-bold">
            {{ favorite.totalKcal }}Kcal
          </div>
        </div>
      </button>
      <button
        class="px-3 flex items-center justify-center border-l text-gray-400 hover:text-red-500 hover:bg-red-50 active:bg-red-50 focus:outline-none"
        @click="() => {
          setDeleteMenuName(favorite.menuName);
          nextTick(() => openModal('beforeDelete'));
        }">
        <span class="material-symbols-outlined">delete</span>
      </button>
    </div>

    <ModalSquare :show="activeModal === 'beforeDelete'" width="80%" @close="closeModal">
      <div class="flex flex-col items-center text-center gap-8">
        <span
          class="material-symbols-outlined flex items-center justify-center w-12 h-12 rounded-full text-4xl text-red-600 bg-red-100">delete</span>
        <p class="text-sm">
          「<span class="font-bold">{{ deleteMenuName }}</span>」を、お気に入りから削除しますか？
        </p>
        <div class="flex w-full gap-4">
          <button class="flex-1 p-2 border border-gray-600 text-gray-600 rounded-full" @click=closeModal>キャンセル</button>
          <button class="flex-1 p-2 bg-red-600 text-white rounded-full" @click="() => {
            openModal('afterDelete');
          }">削除する</button>
        </div>
      </div>
    </ModalSquare>
  </div>
</template>