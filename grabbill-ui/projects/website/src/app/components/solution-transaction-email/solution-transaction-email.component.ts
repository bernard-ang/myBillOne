import { Component } from '@angular/core';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-solution-transaction-email',
  templateUrl: './solution-transaction-email.component.html',
  styleUrls: ['./solution-transaction-email.component.less'],
})
export class SolutionTransactionEmailComponent {
  getRegisterUrl() {
    return environment.config.appRegisterUrl;
  }
}
