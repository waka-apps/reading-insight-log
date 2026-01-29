<script setup lang="ts">
type Book = {
  id: string;
  title: string;
  author?: string;
  insightCount: number;
  updatedAt: string;
};

const books = ref<Book[]>([
  {
    id: "book-1",
    title: "Effective Kotlin",
    author: "Marcin Moskala",
    insightCount: 3,
    updatedAt: "2026-01-29",
  },
  {
    id: "book-2",
    title: "Clean Architecture",
    author: "Robert C. Martin",
    insightCount: 5,
    updatedAt: "2026-01-20",
  },
  {
    id: "book-3",
    title: "The Lean Startup",
    author: "Eric Ries",
    insightCount: 2,
    updatedAt: "2026-01-12",
  },
]);
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Books</h1>
      <NuxtLink to="/books/new" class="px-3 py-2 rounded border">New Book</NuxtLink>
    </header>

    <ul class="space-y-3">
      <li v-for="b in books" :key="b.id" class="border rounded p-4">
        <NuxtLink :to="`/books/${b.id}`" class="block">
          <div class="flex items-start justify-between gap-4">
            <div>
              <div class="font-semibold">{{ b.title }}</div>
              <div class="text-sm opacity-70" v-if="b.author">{{ b.author }}</div>
            </div>
            <div class="text-right text-sm opacity-80">
              <div>{{ b.insightCount }} insights</div>
              <div>Updated: {{ b.updatedAt }}</div>
            </div>
          </div>
        </NuxtLink>
      </li>
    </ul>
  </main>
</template>
