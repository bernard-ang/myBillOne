package com.grabbill.core.service;

import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactGroup;
import com.grabbill.core.entity.User;
import com.grabbill.core.repository.ContactGroupRepository;
import com.grabbill.core.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author michaellow
 */
public class ContactGroupServiceImpl implements ContactGroupService {

    @Autowired
    private ContactGroupRepository repository;

    @Autowired
    private ContactRepository contactRepository;


    @Override
    public Page<ContactGroup> searchContactGroups(
            final User user,
            final String name,
            final Pageable pageable
    ) {
        Integer accountId = user.getAccount().getId();
        if (StringUtils.hasLength(name)) {
            return repository.findByAccountIdAndNameContainingIgnoreCase(accountId, name, pageable);
        }
        return repository.findByAccountId(accountId, pageable);
    }

    @Override
    public Optional<ContactGroup> getById(
            final User user,
            final Integer id
    ) {
        return repository.findByAccountIdAndId(user.getAccount().getId(), id);
    }

    @Override
    public ContactGroup save(final ContactGroup contactGroup) {
        return repository.save(contactGroup);
    }

    @Override
    public void delete(final ContactGroup contactGroup) {
        if (!contactGroup.getContacts().isEmpty()) {
            for (Contact contact : contactGroup.getContacts()) {
                contact.getContactGroups().remove(contactGroup);
                contactRepository.saveAndFlush(contact);
            }
        }

        repository.deleteById(contactGroup.getId());
    }

}
