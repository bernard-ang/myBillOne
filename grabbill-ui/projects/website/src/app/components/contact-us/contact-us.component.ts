import { Component } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-contact-us',
  templateUrl: './contact-us.component.html',
  styleUrls: ['./contact-us.component.less'],
})
export class ContactUsComponent {
  isLoading = false;
  form: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder) {
    this.form = this.fb.group({
      name: [null, [Validators.required]],
      email: [null, [Validators.required, Validators.email]],
      subject: [null, [Validators.required]],
      message: [null, [Validators.required]],
    });
  }

  submitForm() {
    // TODO: send contact us email
    if (this.form.valid) {
      const formValues = this.form.getRawValue();
      const body = `Name: ${formValues.name}\r\nEmail: ${formValues.email}\r\nMessage:${formValues.message}`;
      window.location.href = `mailto:${environment.config.supportEmail}?subject=${
        formValues.subject
      }&body=${encodeURIComponent(body)}`;
    }
  }
}
