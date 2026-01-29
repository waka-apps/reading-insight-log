<script setup lang="ts">
const title = ref("");
const author = ref("");

const canSave = computed(() => title.value.trim().length > 0);

function onSave() {
  const payload = {
    title: title.value.trim(),
    author: author.value.trim() || null,
  };
  console.log("create book:", payload);

  // v1: まずはbooksに戻す（後でAPI接続したら、作成したbookIdへ遷移）
  navigateTo("/books");
}
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink to="/books" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">New Book</h1>
      <p class="opacity-70 mt-1">Create a book to attach insights.</p>
    </header>

    <form class="space-y-4" @submit.prevent="onSave">
      <div>
        <label class="block font-medium mb-1"> Title <span class="text-red-600">*</span> </label>
        <input v-model="title" class="w-full border rounded p-2" />
      </div>

      <div>
        <label class="block font-medium mb-1">Author</label>
        <input v-model="author" class="w-full border rounded p-2" />
      </div>

      <div class="pt-2 flex gap-3">
        <button type="submit" class="px-4 py-2 border rounded" :disabled="!canSave">Save</button>

        <NuxtLink to="/books" class="px-4 py-2 border rounded inline-block"> Cancel </NuxtLink>
      </div>
    </form>
  </main>
</template>
