import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiMessage,
  ApiResponseModel,
  ContactBasicModel,
  ContactModel,
  ContactsBasicPayloadModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ContactStateModel } from './contact.state-model';
import { ContactApi } from '../../api/contact.api';
import {
  BulkDeleteContacts,
  DeleteContact,
  GetContact,
  NewContact,
  QueryContacts,
  QuerySearchContacts,
  ResetContact,
  ResetContacts,
  ResetSearchContacts,
  UpdateContact,
  UpdateContactGroups,
  UploadContacts,
} from './contact.state-actions';

@State<ContactStateModel>({
  name: 'contact',
  defaults: {
    contactPageable: makePageable(10, 1, 'lastModifiedDate', 'DESC'),
    contactSearchResult: makeSearchResultPayload(),
    searchContactPageable: makePageable(10000, 1, 'email', 'ASC'),
    searchContactSearchResult: makeSearchResultPayload(),
    searchFilters: {},
    filters: {},
    updatedContacts: [],
    skipContacts: [],
  },
})
@Injectable()
export class ContactState {
  constructor(private contactApi: ContactApi) {}

  @Selector()
  static contactSearchResult(state: ContactStateModel) {
    return state.contactSearchResult;
  }

  @Selector()
  static contactPageable(state: ContactStateModel) {
    return state.contactPageable;
  }

  @Selector()
  static searchContactPageable(state: ContactStateModel) {
    return state.searchContactPageable;
  }

  @Selector()
  static contact(state: ContactStateModel) {
    return state.contact;
  }

  @Selector()
  static filters(state: ContactStateModel) {
    return state.filters;
  }

  @Selector()
  static email(state: ContactStateModel) {
    return state.email;
  }

  @Selector()
  static updatedContacts(state: ContactStateModel) {
    return state.updatedContacts;
  }

  @Selector()
  static skipContacts(state: ContactStateModel) {
    return state.skipContacts;
  }

  @Selector()
  static searchContactSearchResult(state: ContactStateModel) {
    return state.searchContactSearchResult;
  }

  @Selector()
  static searchEmail(state: ContactStateModel) {
    return state.searchEmail;
  }

  @Selector()
  static searchFilters(state: ContactStateModel) {
    return state.searchFilters;
  }

  @Action(ResetContacts)
  resetContacts(context: StateContext<ContactStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.email = undefined;
        draft.filters = {};
        draft.contactPageable = makePageable(10, 1, 'lastModifiedDate', 'DESC');
        draft.contactSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(ResetSearchContacts)
  resetSearchContacts(context: StateContext<ContactStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.searchEmail = undefined;
        draft.searchFilters = {};
        draft.searchContactPageable = makePageable(10, 1, 'lastModifiedDate', 'DESC');
        draft.searchContactSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryContacts)
  queryContacts(context: StateContext<ContactStateModel>, { pageable, email, filters }: QueryContacts) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.contactPageable = pageable ? pageable : draft.contactPageable;
        draft.email = email !== undefined ? email : draft.email;
        draft.filters = filters ? filters : draft.filters;
      })
    );

    return this.contactApi
      .searchContacts(context.getState().contactPageable, context.getState().email, context.getState().filters)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<ContactBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.contactSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(QuerySearchContacts)
  querySearchContacts(context: StateContext<ContactStateModel>, { pageable, email, filters }: QuerySearchContacts) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.searchContactPageable = pageable ? pageable : draft.contactPageable;
        draft.searchEmail = email !== undefined ? email : draft.email;
        draft.searchFilters = filters ? filters : draft.filters;
      })
    );

    return this.contactApi
      .searchContacts(context.getState().searchContactPageable, context.getState().searchEmail, context.getState().searchFilters)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<ContactBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.searchContactSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(GetContact)
  getContact(context: StateContext<ContactStateModel>, { id }: GetContact) {
    return this.contactApi.getContactDetails(id).pipe(
      tap((response: ApiResponseModel<ContactModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.contact = response.data;
          })
        );
      })
    );
  }

  @Action(ResetContact)
  resetContact(context: StateContext<ContactStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.contact = undefined;
      })
    );
  }

  @Action(NewContact)
  newContact(context: StateContext<ContactStateModel>, { request }: NewContact) {
    return this.contactApi.newContact(request).pipe(
      tap((response: ApiResponseModel<ContactModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.contact = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateContact)
  updateContact(context: StateContext<ContactStateModel>, { id, request }: UpdateContact) {
    return this.contactApi.updateContactDetails(id, request).pipe(
      tap((response: ApiResponseModel<ContactModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.contact = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteContact)
  deleteContact(context: StateContext<ContactStateModel>, { id }: DeleteContact) {
    return this.contactApi.deleteContact(id).pipe(tap(() => context.dispatch(new QueryContacts())));
  }

  @Action(UpdateContactGroups)
  updateContactGroups(context: StateContext<ContactStateModel>, { id, request }: UpdateContactGroups) {
    return this.contactApi.updateContactGroups(id, request).pipe(
      tap((response: ApiResponseModel<ContactModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.contact = response.data;
          })
        );
      })
    );
  }

  @Action(UploadContacts)
  uploadContacts(context: StateContext<ContactStateModel>, { request }: UploadContacts) {
    return this.contactApi.uploadContacts(request).pipe(
      tap((response: ApiResponseModel<ContactsBasicPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.updatedContacts = response.data.contacts;
            const updatedEmails = draft.updatedContacts.map((contact) => contact.email);
            draft.skipContacts = request.contacts.filter((contact) => !updatedEmails.includes(contact.email));
          })
        );
        context.dispatch(new QueryContacts());
      })
    );
  }

  @Action(BulkDeleteContacts)
  bulkDeleteContacts(context: StateContext<ContactStateModel>, { request }: BulkDeleteContacts) {
    return this.contactApi.bulkDeleteContact(request).pipe(
      tap((response: ApiResponseModel<ApiMessage>) => {
        context.dispatch(new QueryContacts());
      })
    );
  }
}
