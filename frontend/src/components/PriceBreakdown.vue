<script setup>
const props = defineProps({
  ingredients : {
    type : Object
  },
  priceBreakdown : {
    type : Object
  }

})
</script>


<template>
  <div>
    <p class="text-sm font-semibold">価格の内訳</p>
    <div class="h-px bg-gray-300 my-2"></div>
    <div class="space-y-1.5 h-60 leading-5 flex-1 overflow-y-auto text-sm">
      <div v-for="(amount, name) in props.ingredients" :key="name"
        class="flex justify-between items-start gap-4">
        <span class="text-left break-words w-48 ml-2">{{ name }}</span>
        <span class="text-left break-words w-24 whitespace-nowrap"><span class="font-medium">{{ props.priceBreakdown[name] }}円</span> / {{ amount }}</span>
      </div>
    </div>
  </div>
</template>