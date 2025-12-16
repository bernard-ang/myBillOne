import { Component } from '@angular/core';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.less'],
})
export class HeaderComponent {
  visible = false;

  open(): void {
    this.visible = true;
  }

  close(): void {
    this.visible = false;
  }

  getLoginUrl() {
    return environment.config.appLoginUrl;
  }

  getRegisterUrl() {
    return environment.config.appRegisterUrl;
  }
}
