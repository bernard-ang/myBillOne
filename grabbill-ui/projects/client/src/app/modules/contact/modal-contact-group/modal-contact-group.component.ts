import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from "@angular/core";
import { UntypedFormBuilder, UntypedFormGroup } from "@angular/forms";
import {
  ContactGroupBasicModel,
  ContactGroupModel,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty
} from "@grabbill/lib";
import { Select, Store } from "@ngxs/store";
import { ContactGroupState } from "../../../../states/contact-group/contact-group.state";
import { Observable } from "rxjs";
import { QueryContactGroups } from "../../../../states/contact-group/contact-group.state-actions";

@Component({
  selector: 'grabbill-client-modal-contact-group',
  templateUrl: './modal-contact-group.component.html',
  styleUrls: ['./modal-contact-group.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalContactGroupComponent implements OnInit, OnChanges {
  @Select(ContactGroupState.contactGroupSearchResult)
  contactGroupSearchResult$!: Observable<SearchResultPayloadModel<ContactGroupBasicModel>>;

  @Input()
  isVisible = false;

  @Input()
  groups!: ContactGroupModel[];

  @Output()
  close = new EventEmitter<void>();

  @Output()
  updateGroup = new EventEmitter<number[]>();

  isLoading = false;

  form: UntypedFormGroup;

  constructor(protected store: Store, private cd: ChangeDetectorRef, private fb: UntypedFormBuilder) {
    this.form = this.fb.group({
      groups: [[]],
    });
  }

  ngOnInit(): void {
    this.store.dispatch(new QueryContactGroups());
  }

  ngOnChanges(changes: SimpleChanges): void {
    const groupChanges = changes['groups'];
    if (groupChanges) {
      const currentValue: ContactGroupModel[] = groupChanges.currentValue;
      this.form.get('groups')?.setValue(currentValue.map((value) => value.id));
    }
  }

  doClose() {
    this.close.emit();
    this.form.get('groups')?.setValue(this.groups.map((value) => value.id));
  }

  doSubmit() {
    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {
      this.updateGroup.emit(this.form.getRawValue().groups);
      this.doClose();
    } else {
      updateAndMarkControlAsDirty(this.form);
    }

    this.isLoading = false;
    this.cd.markForCheck();
  }
}
