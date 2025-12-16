import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCarouselModule } from 'ng-zorro-antd/carousel';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { LayoutComponent } from './components/common/layout/layout.component';
import { HeaderComponent } from './components/common/header/header.component';
import { FooterComponent } from './components/common/footer/footer.component';
import { HomeComponent } from './components/home/home.component';
import { PricingComponent } from './components/pricing/pricing.component';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { ContactUsComponent } from './components/contact-us/contact-us.component';
import { PrivacyPolicyComponent } from './components/privacy-policy/privacy-policy.component';
import { TermsOfServiceComponent } from './components/terms-of-service/terms-of-service.component';
import { SolutionDigitalFilingComponent } from './components/solution-digital-filing/solution-digital-filing.component';
import { SolutionTransactionEmailComponent } from './components/solution-transaction-email/solution-transaction-email.component';
import { SolutionEmailMarketingComponent } from './components/solution-email-marketing/solution-email-marketing.component';
import { NzFormModule } from 'ng-zorro-antd/form';
import { ReactiveFormsModule } from '@angular/forms';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { PlanApi } from '../api/plan.api';
import { PlanService } from '../services/plan.service';
import { HttpClientModule } from '@angular/common/http';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzTableModule } from 'ng-zorro-antd/table';
import { IconsProviderModule } from './icons-provider.module';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';

@NgModule({
  declarations: [
    AppComponent,
    LayoutComponent,
    HomeComponent,
    PricingComponent,
    AboutUsComponent,
    ContactUsComponent,
    HeaderComponent,
    FooterComponent,
    PrivacyPolicyComponent,
    TermsOfServiceComponent,
    SolutionDigitalFilingComponent,
    SolutionTransactionEmailComponent,
    SolutionEmailMarketingComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    AppRoutingModule,
    IconsProviderModule,
    NzLayoutModule,
    NzButtonModule,
    NzCarouselModule,
    NzDropDownModule,
    NzFormModule,
    NzInputModule,
    NzSwitchModule,
    NzSelectModule,
    NzDividerModule,
    NzTableModule,
    NzDrawerModule,
  ],
  providers: [{ provide: PlanApi, useClass: PlanService }],
  bootstrap: [AppComponent],
})
export class AppModule {}
