import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from "@angular/core";
import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { DataType } from "@grabbill/lib";

export interface FilterField {
  label: string;
  name: string;
  type: DataType;
  required: boolean;
  validators?: Validators[];
  options?: {
    label: string;
    value: any;
  }[]
}

@Component({
  selector: 'grabbill-client-modal-filter-form',
  templateUrl: './modal-filter-form.component.html',
  styleUrls: ['./modal-filter-form.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalFilterFormComponent implements OnChanges {
  @Input()
  isVisible = false;

  @Input()
  fields!: FilterField[];

  @Input()
  filters: { [index: string]: any } = {};

  @Output()
  filterEvent = new EventEmitter<{ [index: string]: any }>();

  @Output()
  close = new EventEmitter<void>();

  isLoading = false;
  isInitialize = false;
  form: UntypedFormGroup;

  constructor(private cd: ChangeDetectorRef, private fb: UntypedFormBuilder) {
    this.form = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      options: [],
      textValue: [undefined, [Validators.required, Validators.maxLength(255)]],
      numberValue: [undefined, [Validators.required, Validators.max(99999999999)]],
      dateValue: [undefined, [Validators.required]],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const isVisibleChanges = changes['isVisible'];
    if (isVisibleChanges) {
      if (isVisibleChanges.previousValue === false && isVisibleChanges.currentValue === true) {
        this.form.get('field')!.setValue(this.fields[0].name);
        this.doFieldChange(this.fields[0].name);
      }
    }
  }

  doClose() {
    this.close.emit();
    this.form.reset();
  }

  doFieldChange(value: string) {
    if (value) {
      const targetField = this.fields.filter((field) => field.name === value)[0];

      this.form.get('type')!.setValue(targetField.type);
      this.form.get('options')!.setValue(targetField.options);
      this.cd.markForCheck();
    }
  }

  doFilter() {
    this.isLoading = true;
    this.cd.markForCheck();

    let updateFilters = { ...this.filters };
    const field = this.form.get('field')!.value;
    const type = this.form.get('type')!.value;
    if (type === DataType.MULTIPLE) {
      updateFilters[field] = this.form.get('textValue')!.value.join(',');
    } else if (type === DataType.TEXT || type === DataType.SELECT) {
      updateFilters[field] = this.form.get('textValue')!.value;
    } else if (type === DataType.NUMBER) {
      updateFilters[field] = this.form.get('numberValue')!.value;
    } else if (type === DataType.DATE) {
      const dateValue = this.form.get('dateValue')!.value;
      const targetDate = new Date(dateValue);
      targetDate.setHours(0, 0, 0, 0);
      updateFilters[field] = targetDate;
    }

    this.filters = updateFilters;

    this.close.emit();
    this.cd.markForCheck();

    this.filterEvent.emit(this.filters);
  }
}
