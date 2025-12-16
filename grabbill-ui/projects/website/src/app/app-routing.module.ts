import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './components/common/layout/layout.component';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { HomeComponent } from './components/home/home.component';
import { ContactUsComponent } from './components/contact-us/contact-us.component';
import { PricingComponent } from './components/pricing/pricing.component';
import { SolutionTransactionEmailComponent } from './components/solution-transaction-email/solution-transaction-email.component';
import { SolutionDigitalFilingComponent } from './components/solution-digital-filing/solution-digital-filing.component';
import { SolutionEmailMarketingComponent } from './components/solution-email-marketing/solution-email-marketing.component';
import { PrivacyPolicyComponent } from './components/privacy-policy/privacy-policy.component';
import { TermsOfServiceComponent } from './components/terms-of-service/terms-of-service.component';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: 'home', component: HomeComponent },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'digital-filing', component: SolutionDigitalFilingComponent },
      { path: 'transactional-email', component: SolutionTransactionEmailComponent },
      { path: 'email-marketing', component: SolutionEmailMarketingComponent },
      { path: 'pricing', component: PricingComponent },
      { path: 'contact-us', component: ContactUsComponent },
      { path: 'about-us', component: AboutUsComponent },
      { path: 'privacy-policy', component: PrivacyPolicyComponent },
      { path: 'terms-of-service', component: TermsOfServiceComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
