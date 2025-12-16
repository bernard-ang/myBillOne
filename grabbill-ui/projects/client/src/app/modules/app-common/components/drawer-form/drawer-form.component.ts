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
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { DataType, resolveErrorMessage, updateAndMarkControlAsDirty } from "@grabbill/lib";

export interface FormField {
  label: string;
  name: string;
  type: DataType;
  required: boolean;
  validators?: Validators[];
  value?: any;
  options?: {
    value: any;
    label: string;
  }[]
}

@Component({
  selector: "grabbill-client-drawer-form",
  templateUrl: "./drawer-form.component.html",
  styleUrls: [ "./drawer-form.component.less" ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DrawerFormComponent implements OnChanges {

  @Input()
  title!: string;

  @Input()
  buttonLabel!: string;

  @Input()
  fields: FormField[] = [];

  @Input()
  isVisible = false;

  @Input()
  reset = true;

  @Output()
  submit = new EventEmitter<any>();

  @Output()
  close = new EventEmitter<void>();

  isLoading = false;
  isInitialize = false;
  form: UntypedFormGroup;

  constructor (private cd: ChangeDetectorRef,
               private fb: UntypedFormBuilder) {
    this.form = this.fb.group({});
  }

  ngOnChanges (changes: SimpleChanges): void {
    if(changes['fields']){
      let formConfig: { [key: string]: any } = {};
      for (const field of this.fields) {
        const validators = [];
        if (field.required) {
          validators.push(Validators.required);
        }
        if (field.validators) {
          for (const validator of field.validators) {
            validators.push(validator);
          }
        }
        formConfig[field.name] = [ field.value, validators ];
      }
      this.form = this.fb.group(formConfig);
      this.cd.markForCheck();
    }
  }

  doClose() {
    this.close.emit();
    if(this.reset){
      this.form.reset();
    }
  }

  getErrorMessage (control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doSubmit() {
    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {
      this.submit.emit(this.form.getRawValue());
      this.doClose();
    } else {
      updateAndMarkControlAsDirty(this.form);
    }

    this.isLoading = false;
    this.cd.markForCheck();
  }

}
