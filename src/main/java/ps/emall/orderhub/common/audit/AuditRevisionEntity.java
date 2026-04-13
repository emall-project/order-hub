package ps.emall.orderhub.common.audit;

import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@RevisionEntity
@Table(name = "revinfo", schema = "audit")
public class AuditRevisionEntity {

    @Id
    @RevisionNumber
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "revinfo_seq_generator"
    )
    @SequenceGenerator(
            name = "revinfo_seq_generator",
            sequenceName = "audit.revinfo_seq",
            allocationSize = 50
    )
    @Column(name = "rev")
    private int rev;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private Long timestamp;

    public int getRev() { return rev; }
    public void setRev(int rev) { this.rev = rev; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}