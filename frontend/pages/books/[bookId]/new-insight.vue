<script setup lang="ts">
const route = useRoute();
const bookId = computed(() => String(route.params.bookId));

const quote = ref("");
const interpretation = ref("");
const tagsText = ref(""); // "kotlin, jvm" みたいにまずは文字列でOK

const canSave = computed(() => quote.value.trim() && interpretation.value.trim());

function onSave() {
  // v1: まずはログに出すだけ
  console.log({
    bookId: bookId.value,
    quote: quote.value,
    interpretation: interpretation.value,
    tags: tagsText.value
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean),
  });
  // 後でAPIにつなぐ
}
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink :to="`/books/${bookId}`" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">New Insight</h1>
      <p class="opacity-80 mt-1">for bookId: {{ bookId }}</p>
    </header>

    <form class="space-y-4" @submit.prevent="onSave">
      <div>
        <label class="block font-medium mb-1">Quote (required)</label>
        <textarea v-model="quote" rows="4" class="w-full border rounded p-2" />
      </div>

      <div>
        <label class="block font-medium mb-1">Interpretation (required)</label>
        <textarea v-model="interpretation" rows="4" class="w-full border rounded p-2" />
      </div>

      <div>
        <label class="block font-medium mb-1">Tags (comma-separated)</label>
        <input v-model="tagsText" class="w-full border rounded p-2" placeholder="kotlin, jvm, entrypoint" />
      </div>

      <button type="submit" class="px-4 py-2 rounded border" :disabled="!canSave">Save</button>
    </form>
  </main>
</template>
