import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ContactRequestModel } from '@grabbill/lib';
import { ContactRowError } from '../../../../utils/validate-contacts';

@Component({
  selector: 'grabbill-client-contact-row-error-modal',
  templateUrl: './contact-row-error-modal.component.html',
  styleUrls: ['./contact-row-error-modal.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactRowErrorModalComponent implements OnInit {
  @Input()
  isVisible = false;

  @Input()
  contacts: ContactRequestModel[] = [];

  @Input()
  contactRowErrors: ContactRowError[] = [];

  @Output()
  close = new EventEmitter<void>();

  constructor() {}

  ngOnInit(): void {}

  doCloseContactRowErrorModal() {
    this.close.emit();
  }
}
