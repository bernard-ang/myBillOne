package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactField;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.DataType;
import com.grabbill.core.repository.ContactFieldRepository;
import com.grabbill.core.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class ContactFieldServiceImpl implements ContactFieldService {

    @Autowired
    private ContactFieldRepository repository;

    @Autowired
    private ContactRepository contactRepository;


    @Override
    public List<ContactField> getAll(final User user) {
        return repository.findByAccountId(user.getAccount().getId());
    }

    @Override
    public List<ContactField> getAll(final Account account) {
        return repository.findByAccountId(account.getId());
    }

    @Override
    public Optional<ContactField> getById(
            final User user,
            final Integer id
    ) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public ContactField save(final ContactField contactField) {
        return repository.save(contactField);
    }

    @Override
    public List<ContactField> saveAll(final List<ContactField> contactFields) {
        return repository.saveAll(contactFields);
    }

    @Override
    public void delete(final ContactField contactField) {
        List<Contact> contacts = contactRepository.findByAccountId(contactField.getAccount().getId());
        for (Contact contact : contacts) {
            for(int i = contactField.getSeqOrder(); i < 10; i++) {
                if (i == 1) {
                    contact.setText1(contact.getText2());
                    contact.setNumber1(contact.getNumber2());
                    contact.setDate1(contact.getDate2());

                } else if (i == 2) {
                    contact.setText2(contact.getText3());
                    contact.setNumber2(contact.getNumber3());
                    contact.setDate2(contact.getDate3());

                } else if (i == 3) {
                    contact.setText3(contact.getText4());
                    contact.setNumber3(contact.getNumber4());
                    contact.setDate3(contact.getDate4());

                } else if (i == 4) {
                    contact.setText4(contact.getText5());
                    contact.setNumber4(contact.getNumber5());
                    contact.setDate4(contact.getDate5());

                } else if (i == 5) {
                    contact.setText5(contact.getText6());
                    contact.setNumber5(contact.getNumber6());
                    contact.setDate5(contact.getDate6());

                } else if (i == 6) {
                    contact.setText6(contact.getText7());
                    contact.setNumber6(contact.getNumber7());
                    contact.setDate6(contact.getDate7());

                } else if (i == 7) {
                    contact.setText7(contact.getText8());
                    contact.setNumber7(contact.getNumber8());
                    contact.setDate7(contact.getDate8());

                } else if (i == 8) {
                    contact.setText8(contact.getText9());
                    contact.setNumber8(contact.getNumber9());
                    contact.setDate8(contact.getDate9());

                } else if (i == 9) {
                    contact.setText9(contact.getText10());
                    contact.setNumber9(contact.getNumber10());
                    contact.setDate9(contact.getDate10());
                }
            }

            contactRepository.saveAndFlush(contact);
        }

        repository.delete(contactField);
    }

    @Override
    public void deleteAll(final List<ContactField> contactFields) {
        repository.deleteAll(contactFields);
    }

    @Override
    public List<ContactField> createDefaultContactFields(final Account account) {
        ContactField firstNameContactField = new ContactField();
        firstNameContactField.setSeqOrder(1);
        firstNameContactField.setName(DEFAULT_FIRST_NAME_FIELD);
        firstNameContactField.setLabel(DEFAULT_FIRST_NAME_FIELD_LABEL);
        firstNameContactField.setDataType(DataType.TEXT);
        firstNameContactField.setRequired(false);
        firstNameContactField.setReferenced(false);
        firstNameContactField.setAccount(account);

        ContactField lastNameContactField = new ContactField();
        lastNameContactField.setSeqOrder(2);
        lastNameContactField.setName(DEFAULT_LAST_NAME_FIELD);
        lastNameContactField.setLabel(DEFAULT_LAST_NAME_FIELD_LABEL);
        lastNameContactField.setDataType(DataType.TEXT);
        lastNameContactField.setRequired(false);
        lastNameContactField.setReferenced(false);
        lastNameContactField.setAccount(account);

        List<ContactField> results = new ArrayList<>();
        results.add(repository.save(firstNameContactField));
        results.add(repository.save(lastNameContactField));
        return results;
    }
}
