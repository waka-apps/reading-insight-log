export type Book = {
  id: string;
  title: string;
  author: string | null;
  updatedAt: string;
  insightCount: number;
};

export type CreateBookRequest = {
  title: string;
  author?: string | null;
};
