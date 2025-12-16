import { Component } from '@angular/core';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-solution-digital-filing',
  templateUrl: './solution-digital-filing.component.html',
  styleUrls: ['./solution-digital-filing.component.less'],
})
export class SolutionDigitalFilingComponent {
  getRegisterUrl() {
    return environment.config.appRegisterUrl;
  }
}
