package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.exception.GrabbillSftpException;
import com.jcraft.jsch.ChannelSftp;

import java.util.Properties;

/**
 * @author michaellow
 */
public interface SftpService {

    Properties CONFIG = new Properties();


    ChannelSftp connect(Account account) throws GrabbillSftpException;

    ChannelSftp connect(String host, int port, String username, String password) throws GrabbillSftpException;

    void changeDirectory(ChannelSftp channelSftp, String path) throws GrabbillSftpException;

    void disconnect(ChannelSftp channelSftp) throws GrabbillSftpException;

}
