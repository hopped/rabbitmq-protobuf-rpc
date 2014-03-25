package Runner;

use strict;
use warnings;
use vars qw(@ISA $AUTOLOAD $VERSION);

$VERSION = '1.0';

use Exporter;

require DynaLoader;
require AutoLoader;

@ISA = qw(DynaLoader Exporter);

bootstrap Runner $VERSION;

1;

__END__

