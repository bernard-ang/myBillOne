package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.exception.GrabbillSftpException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michaellow
 */
@Slf4j
public class SftpServiceImpl implements SftpService {

    static { CONFIG.put("StrictHostKeyChecking", "no"); }
    private final JSch jsch = new JSch();


    @Override
    public ChannelSftp connect(final Account account) throws GrabbillSftpException {
        return connect(
                account.getSftpHost(),
                account.getSftpPort(),
                account.getSftpUsername(),
                account.getSftpPassword()
        );
    }

    @Override
    public ChannelSftp connect(
            final String host,
            final int port,
            final String username,
            final String password
    ) throws GrabbillSftpException {
        try {
            Session jschSession = jsch.getSession(username, host, port);
            jschSession.setConfig(CONFIG);
            jschSession.setPassword(password);
            jschSession.connect();

            ChannelSftp channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
            channelSftp.connect();
            return channelSftp;
        } catch (JSchException e) {
            String errorMsg = "Failed to connect to SFTP server";
            log.warn(errorMsg, e);
            throw new GrabbillSftpException(errorMsg, e);
        }
    }

    @Override
    public void changeDirectory(final ChannelSftp channelSftp, final String path) throws GrabbillSftpException {
        try {
            channelSftp.cd(path);
        } catch (SftpException e) {
            throw new GrabbillSftpException("Failed to load folder - " + path, e);
        }
    }

    @Override
    public void disconnect(final ChannelSftp channelSftp) throws GrabbillSftpException {
        try {
            channelSftp.disconnect();
            channelSftp.getSession().disconnect();
            channelSftp.exit();
        } catch (JSchException e) {
            throw new GrabbillSftpException("Failed to disconnect SFTP session", e);
        }
    }

}
