<script>
export default {
  name: "ModalSquare",
  props: {
    show: { type: Boolean, required: true },
  },
  emits: ["close"],
  methods: {
    close() {
      this.$emit("close");
    },
  },
};
</script>

<template>
  <div
    v-if="show"
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
    @click.self="close"
  >
    <div class="relative bg-white rounded-lg shadow-lg w-80 p-6">
      <!-- 閉じるボタン -->
      <button
        class="absolute top-2 right-2 text-xl text-gray-400 hover:text-gray-600"
        @click="close"
      >
        ✕
      </button>

      <!-- モーダルの中身をスロットで受け取る -->
      <slot></slot>
    </div>
  </div>
</template>