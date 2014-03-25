#ifdef __cplusplus
extern "C" {
#endif
#include "EXTERN.h"
#include "perl.h"
#include "XSUB.h"
#ifdef __cplusplus
}
#endif

// Taken from Glib and Gtk

static void
call_XS ( pTHX_ void (*subaddr) (pTHX_ CV *), CV * cv, SV ** mark )
{
  dSP;
  PUSHMARK (mark);
  (*subaddr) (aTHX_ cv);
  PUTBACK;
}

#define CALL_BOOT(name)	call_XS (aTHX_ name, cv, mark)

// We need these declarations with "C" linkage

#ifdef __cplusplus
extern "C" {
#endif
  XS(boot_Runner__AuthRequest);
  XS(boot_Runner__AuthResult);
  XS(boot_Runner__Pair);
  XS(boot_Runner__ProtoMap);
  XS(boot_Runner__Run);
  XS(boot_Runner__RunObject);
  XS(boot_Runner__RunRequest);
  XS(boot_Runner__Track);
  XS(boot_Runner__TrackPoint);
  XS(boot_Runner__User);
#ifdef __cplusplus
}
#endif

// Bootstrap this module by bootstrapping all of the others.

MODULE = Runner	PACKAGE = Runner

BOOT:
  CALL_BOOT(boot_Runner__AuthRequest);
  CALL_BOOT(boot_Runner__AuthResult);
  CALL_BOOT(boot_Runner__Pair);
  CALL_BOOT(boot_Runner__ProtoMap);
  CALL_BOOT(boot_Runner__Run);
  CALL_BOOT(boot_Runner__RunObject);
  CALL_BOOT(boot_Runner__RunRequest);
  CALL_BOOT(boot_Runner__Track);
  CALL_BOOT(boot_Runner__TrackPoint);
  CALL_BOOT(boot_Runner__User);
