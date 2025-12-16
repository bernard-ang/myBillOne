import { Component } from '@angular/core';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-solution-email-marketing',
  templateUrl: './solution-email-marketing.component.html',
  styleUrls: ['./solution-email-marketing.component.less'],
})
export class SolutionEmailMarketingComponent {
  getRegisterUrl() {
    return environment.config.appRegisterUrl;
  }
}
