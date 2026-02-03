<script setup lang="ts">
import type { Book } from "~/types/book";

const api = useApi();

const pending = ref(true);
const error = ref<string | null>(null);
const books = ref<Book[]>([]);

async function load() {
  pending.value = true;
  error.value = null;
  try {
    books.value = await api.get<Book[]>("/admin/books");
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    pending.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">Books</h1>
        <p class="opacity-70 mt-1">API: {{ useRuntimeConfig().public.apiBaseUrl }}</p>
      </div>

      <NuxtLink to="/books/new" class="px-4 py-2 border rounded"> New Book </NuxtLink>
    </header>

    <section v-if="pending" class="border rounded p-4">Loading...</section>

    <section v-else-if="error" class="border rounded p-4">
      <p class="font-semibold">Error</p>
      <p class="opacity-80 mt-1">{{ error }}</p>
      <button class="mt-4 px-4 py-2 border rounded" @click="load">Retry</button>
    </section>

    <section v-else class="space-y-3">
      <div v-if="books.length === 0" class="border rounded p-4 opacity-80">No books yet.</div>

      <ul class="space-y-3">
        <li v-for="b in books" :key="b.id" class="border rounded p-4 hover:bg-gray-50 transition">
          <NuxtLink :to="`/books/${b.id}`" class="block">
            <div class="flex items-start justify-between gap-4">
              <div>
                <div class="font-semibold">{{ b.title }}</div>
                <div v-if="b.author" class="text-sm opacity-70">{{ b.author }}</div>
              </div>
              <div class="text-sm opacity-70">{{ b.insightCount }} insights</div>
            </div>

            <div class="text-xs opacity-60 mt-2">updatedAt: {{ b.updatedAt }}</div>
          </NuxtLink>
        </li>
      </ul>
    </section>
  </main>
</template>
