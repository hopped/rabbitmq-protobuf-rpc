use ExtUtils::MakeMaker;

$CC = 'g++';

# Generate the C++, XS, and Perl files that we need.
my $protoxs = "protoxs --cpp_out=. --out=target Runner.proto";

print "Generating C++ and Perl/XS sources...\n";
print "$protoxs\n";

`$protoxs`;
`rm -f target/*.pm`;
`mv target/*.xs .`;

# Now write the Makefile.
WriteMakefile(
    'NAME'	        => 'Runner',
	'VERSION_FROM'  => 'Runner.pm',
	'OPTIMIZE'      => '-O2 -Wall',
    'CC'            => $CC,
    'LD'            => '$(CC)',
    'CCFLAGS'       => '-fno-strict-aliasing',
	'OBJECT'        => 'Runner.pb.o Runner.o AuthResult.o RunRequest.o AuthRequest.o User.o Run.o RunList.o Pair.o ProtoMap.o RunObject.o Track.o TrackPoint.o',
    'INC'           =>  "",
	'LIBS'          => [" -L/usr/local/lib -lprotobuf"],
    'XSOPT'         => '-C++',
    'LDDLFLAGS'     => '-arch x86_64 -bundle -undefined dynamic_lookup -L/usr/local/lib -fstack-protector',
);
