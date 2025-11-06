<script setup>
defineProps({
  modelValue: String
})
defineEmits(['update:modelValue'])
</script>


<template>
  <!-- フッター全体 -->
  <div class="sticky bottom-0 flex justify-between items-center px-8 h-20 bg-white">
    <!-- 左：データタブ -->
    <button @click="$emit('update:modelValue', 'data')"
      :class="[
      'flex flex-col items-center justify-center w-20 h-24 rounded-2xl transition-all duration-150',
      modelValue === 'data' ? 'text-green-600' : 'text-gray-600'
    ]">
      <span class="material-symbols-outlined">database_search</span>
      <span class="text-label-medium font-medium">データ</span>
    </button>
    <!-- 中央：献立タブ -->
    <button @click="$emit('update:modelValue', 'meal')"
      :class="[
      'flex flex-col items-center justify-center w-20 h-24 rounded-2xl transition-all duration-150',
      modelValue === 'meal' ? 'text-green-600' : 'text-gray-600'
    ]">
      <span class="material-symbols-outlined">dinner_dining</span>
      <span class="text-label-medium font-medium">献立</span>
    </button>
    <!-- 右：履歴タブ -->
    <button @click="$emit('update:modelValue', 'history')"
      :class="[
      'flex flex-col items-center justify-center w-20 h-24 rounded-2xl transition-all duration-150',
      modelValue === 'history' ? 'text-green-600' : 'text-gray-600'
    ]">
      <span class="material-symbols-outlined">history</span>
      <span class="text-label-medium font-medium">履歴</span>
    </button>
  </div>
</template>
