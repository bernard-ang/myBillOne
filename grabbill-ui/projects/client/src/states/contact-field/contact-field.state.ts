import produce from "immer";
import { Injectable } from "@angular/core";
import { tap } from "rxjs";
import { Action, Selector, State, StateContext } from "@ngxs/store";
import { ApiResponseModel, ContactFieldsPayloadModel } from "@grabbill/lib";
import { ContactFieldStateModel } from "./contact-field-state.model";
import { ContactFieldApi } from "../../api/contact-field.api";
import { GetContactFields, ResetContactFields, UpdateContactFields } from "./contact-field.state-actions";

@State<ContactFieldStateModel>({
  name: 'contact_field',
  defaults: {
    contactFields: [],
    isFieldInitialize: false,
  },
})
@Injectable()
export class ContactFieldState {
  constructor(private contactFieldApi: ContactFieldApi) {}

  @Selector()
  static contactFields(state: ContactFieldStateModel) {
    return state.contactFields;
  }

  @Selector()
  static isFieldInitialize(state: ContactFieldStateModel) {
    return state.isFieldInitialize;
  }

  @Action(ResetContactFields)
  resetContactGroups(context: StateContext<ContactFieldStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.contactFields = [];
        draft.isFieldInitialize = false;
      })
    );
  }

  @Action(GetContactFields)
  queryContactGroups(context: StateContext<ContactFieldStateModel>) {
    return this.contactFieldApi.getContactFields().pipe(
      tap((response: ApiResponseModel<ContactFieldsPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.isFieldInitialize = true;
            draft.contactFields = response.data.contactFieldPayloads;
          })
        );
      })
    );
  }

  @Action(UpdateContactFields)
  updateContactGroup(context: StateContext<ContactFieldStateModel>, { request }: UpdateContactFields) {
    return this.contactFieldApi.saveContactFields(request).pipe(
      tap((response: ApiResponseModel<ContactFieldsPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.contactFields = response.data.contactFieldPayloads;
          })
        );
      })
    );
  }
}
