import { Routes } from '@angular/router';
import { PaymentCheckoutComponent } from './components/payment-checkout.component';
import { PaymentSuccessComponent } from './components/payment-success.component';
import { InvoiceComponent } from './components/invoice.component';

export const PAYMENTS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'invoices',
    pathMatch: 'full'
  },
  {
    path: 'checkout/:id',
    component: PaymentCheckoutComponent
  },
  {
    path: 'success',
    component: PaymentSuccessComponent
  },
  {
    path: 'invoices',
    component: InvoiceComponent
  }
];
