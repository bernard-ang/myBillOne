import { Component } from '@angular/core';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.less'],
})
export class FooterComponent {
  getFacebookUrl() {
    return environment.config.facebook;
  }

  getInstagramUrl() {
    return environment.config.instagram;
  }

  getLinkedInUrl() {
    return environment.config.linkedin;
  }

  getSupportEmail() {
    return environment.config.supportEmail;
  }

  hasSocialGroup() {
    return environment.config.socialGroup;
  }
}
