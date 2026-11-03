package exnihiloauto.fabric;
public enum MachineKind {
    SIEVE("auto_sieve"), HAMMER("auto_hammer"), SILKER("auto_silker");
    public final String id;
    MachineKind(String id){this.id=id;}
}
