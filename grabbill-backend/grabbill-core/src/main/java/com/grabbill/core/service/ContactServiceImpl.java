package com.grabbill.core.service;

import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactField;
import com.grabbill.core.entity.User;
import com.grabbill.core.repository.ContactFieldRepository;
import com.grabbill.core.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository repository;

    @Autowired
    private ContactFieldRepository contactFieldRepository;


    @Override
    public Page<Contact> searchByFilters(
            final User user,
            final Map<String, String> filters,
            final Pageable pageable
    ) {
        Integer accountId = user.getAccount().getId();
        List<ContactField> contactFields = contactFieldRepository.findByAccountId(accountId);
        return repository.searchByFilters(accountId, filters, contactFields, pageable);
    }

    @Override
    public Optional<Contact> getById(
            final User user,
            final Integer id
    ) {
        return repository.findByAccountIdAndId(user.getAccount().getId(), id);
    }

    @Override
    public boolean existsByEmail(
            final User user,
            final String email
    ) {
        return repository.existsByAccountIdAndEmail(user.getAccount().getId(), email);
    }

    @Override
    public List<Contact> getByEmail(
            final User user,
            final String email
    ) {
        return repository.findByAccountIdAndEmail(user.getAccount().getId(), email);
    }

    @Override
    public List<Contact> getAll(final User user) {
        return repository.findByAccountId(user.getAccount().getId());
    }

    @Override
    public Contact save(final Contact contact) {
        return repository.save(contact);
    }

    @Override
    public List<Contact> saveAll(final List<Contact> contacts) {
        return repository.saveAll(contacts);
    }

    @Override
    public void delete(final Contact contact) {
        contact.getContactGroups().clear();
        repository.saveAndFlush(contact);

        repository.deleteById(contact.getId());
    }

    @Override
    public void deleteAllByIds(final User user, final List<Integer> ids) {
        repository.deleteByAccountIdAndIdIn(user.getAccount().getId(), ids);
    }

    public long count() {
        return repository.count();
    }

}
