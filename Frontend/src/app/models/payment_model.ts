export interface PaymentToDto {
  userId: number;
  courseId?: number;
  packageId?: number;
  paymentType: string;
  amount: number;
  promotionCode?: string;
  subscriptionDurationMonths?: number;
  cardNumber: string;
  cvv: string;
  expirationDate: string;
  cardHolderName: string;
}

export interface PaymentFromDto {
  id: number;
  userId: number;
  userEmail: string;
  courseId?: number;
  courseName?: string;
  packageId?: number;
  packageName?: string;
  paymentType: string;
  amount: number;
  discountAmount?: number;
  finalAmount: number;
  promotionCode?: string;
  status: string;
  paymentDate: string;
  subscriptionDurationMonths?: number;
  stripePaymentIntentId?: string;
  stripeSessionId?: string;
  createdAt: string;
  updatedAt: string;
}
