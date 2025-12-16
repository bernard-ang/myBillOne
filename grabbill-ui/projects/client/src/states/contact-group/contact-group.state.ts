import produce from 'immer';
import { Injectable } from '@angular/core';
import { of, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  ContactGroupBasicModel,
  ContactGroupBindContactsPayloadModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ContactGroupStateModel } from './contact-group.state-model';
import { ContactGroupApi } from '../../api/contact-group.api';
import {
  DeleteContactGroup,
  LoadMoreContactGroups,
  NewContactGroup,
  QueryContactGroups,
  ResetContactGroups,
  UpdateContactGroup,
  UploadContactGroupContact,
} from './contact-group.state-actions';

@State<ContactGroupStateModel>({
  name: 'contact_group',
  defaults: {
    contactGroupPageable: makePageable(50, 1, 'name', 'ASC'),
    contactGroupSearchResult: makeSearchResultPayload(),
    isGroupInitialize: false,
    emailsUpdated: [],
    invalidEmails: [],
  },
})
@Injectable()
export class ContactGroupState {
  constructor(private contactGroupApi: ContactGroupApi) {}
  @Selector()
  static contactGroupSearchResult(state: ContactGroupStateModel) {
    return state.contactGroupSearchResult;
  }

  @Selector()
  static contactGroupPageable(state: ContactGroupStateModel) {
    return state.contactGroupPageable;
  }

  @Selector()
  static contactGroupName(state: ContactGroupStateModel) {
    return state.name;
  }

  @Selector()
  static emailsUpdated(state: ContactGroupStateModel) {
    return state.emailsUpdated;
  }

  @Selector()
  static invalidEmails(state: ContactGroupStateModel) {
    return state.invalidEmails;
  }

  @Selector()
  static isGroupInitialize(state: ContactGroupStateModel) {
    return state.isGroupInitialize;
  }

  @Action(ResetContactGroups)
  resetContactGroups(context: StateContext<ContactGroupStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.contactGroupSearchResult = makeSearchResultPayload();
        draft.contactGroupPageable = makePageable();
        draft.name = undefined;
        draft.isGroupInitialize = false;
      })
    );
  }

  @Action(QueryContactGroups)
  queryContactGroups(context: StateContext<ContactGroupStateModel>, { pageable, name }: QueryContactGroups) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.contactGroupPageable = pageable ? pageable : draft.contactGroupPageable;
        draft.name = name !== undefined ? name : draft.name;
      })
    );

    return this.contactGroupApi.getContactGroups(context.getState().contactGroupPageable, context.getState().name).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<ContactGroupBasicModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.contactGroupSearchResult = response.data;
            draft.isGroupInitialize = true;
          })
        );
      })
    );
  }

  @Action(LoadMoreContactGroups)
  loadMoreDigitalFilingTypes(context: StateContext<ContactGroupStateModel>) {
    const pageable = context.getState().contactGroupPageable;
    const page = pageable.page;
    const totalPages = context.getState().contactGroupSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.contactGroupPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.contactGroupApi
        .getContactGroups(context.getState().contactGroupPageable, context.getState().name)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<ContactGroupBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.contactGroupSearchResult = {
                  items: [...draft.contactGroupSearchResult.items, ...response.data.items],
                  totalItems: response.data.totalItems,
                  totalPages: response.data.totalPages,
                };
              })
            );
          })
        );
    } else {
      return of(null);
    }
  }

  @Action(NewContactGroup)
  newContactGroup(context: StateContext<ContactGroupStateModel>, { request }: NewContactGroup) {
    return this.contactGroupApi.newContactGroup(request).pipe(tap(() => context.dispatch(new QueryContactGroups())));
  }

  @Action(UpdateContactGroup)
  updateContactGroup(context: StateContext<ContactGroupStateModel>, { id, request }: UpdateContactGroup) {
    return this.contactGroupApi
      .updateContactGroup(id, request)
      .pipe(tap(() => context.dispatch(new QueryContactGroups())));
  }

  @Action(DeleteContactGroup)
  deleteContactGroup(context: StateContext<ContactGroupStateModel>, { id }: DeleteContactGroup) {
    return this.contactGroupApi.deleteContactGroup(id).pipe(tap(() => context.dispatch(new QueryContactGroups())));
  }

  @Action(UploadContactGroupContact)
  uploadContactGroupContact(context: StateContext<ContactGroupStateModel>, { id, request }: UploadContactGroupContact) {
    return this.contactGroupApi.updateContactGroupContact(id, request).pipe(
      tap((response: ApiResponseModel<ContactGroupBindContactsPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailsUpdated = response.data.emailsUpdated;
            draft.invalidEmails = response.data.invalidEmails;
          })
        );
      })
    );
  }
}
