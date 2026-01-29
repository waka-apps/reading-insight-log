<script setup lang="ts">
const route = useRoute();
const bookId = computed(() => String(route.params.bookId));
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink to="/books" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">Book Detail</h1>
      <p class="opacity-80 mt-1">bookId: {{ bookId }}</p>
    </header>

    <section class="border rounded p-4">
      <p class="opacity-70">This page will show insights for the book.</p>
      <div class="mt-4">
        <NuxtLink :to="`/books/${bookId}/new-insight`" class="px-3 py-2 rounded border inline-block">
          + New Insight
        </NuxtLink>
      </div>
    </section>
  </main>
</template>
