export type Insight = {
  id: string;
  bookId: string;
  quote: string;
  interpretation: string;
  tags?: string[];
  createdAt: string;
  nextReviewAt: string;
  reviewIntervalDays: number;
};

export type CreateInsightRequest = { quote: string; interpretation: string; tags?: string[] };
